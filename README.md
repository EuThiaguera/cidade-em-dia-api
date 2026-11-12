# Cidade em Dia — API REST

API RESTful do **Cidade em Dia**, plataforma de inteligência urbana e participação cidadã
(Global Solution 2026/2 — CitizenTech — Domain Driven Design Using Java, FIAP).

Moradores registram problemas urbanos (buracos, alagamentos, iluminação...), outros moradores
**confirmam** que o problema continua, e a gestão pública registra a **linha do tempo** até a conclusão.

## Integrantes

| Nome | RM | Turma |
|------|----|-------|
| Thiago Andrade | RM_____ | 1TDS |
| | | |
| | | |

## Links

- API no Render: `https://SEU-SERVICO.onrender.com`
- Vídeo (YouTube não listado): `https://youtu.be/...`

## Tecnologias

Java 21 · Spring Boot 3.5 · JDBC puro (DataSource + PreparedStatement) · Oracle FIAP ·
Bean Validation · ModelMapper · Docker · Render

## Regras de negócio (além do CRUD)

1. **Ocorrência duplicada (409):** não é possível registrar uma ocorrência se já existir outra **aberta**,
   da **mesma categoria**, a até **50 metros** (distância calculada com a fórmula de Haversine).
   A resposta traz `ocorrenciaExistenteId` para o front sugerir a confirmação.
2. **Ciclo de vida do status (409):** `REGISTRADA → EM_ANALISE → EM_EXECUCAO → CONCLUIDA`
   (ou `DESCARTADA` a partir de REGISTRADA/EM_ANALISE). Não é possível pular etapas nem reabrir.
   Para **concluir** é obrigatório enviar `urlEvidencia` (foto do depois).
3. **Confirmação comunitária (409):** só em ocorrência aberta, o autor não confirma o próprio registro
   e cada morador confirma uma única vez.
4. **Remoção com filhos (409):** categoria, morador ou ocorrência com registros vinculados não podem ser removidos.
   Só a atualização **mais recente** pode ser removida (e a ocorrência volta ao status anterior).

## Endpoints

| Método | Rota | Sucesso | Erros |
|--------|------|---------|-------|
| GET | `/` | 200 | — |
| GET | `/categorias` | 200 | — |
| GET | `/categorias/{id}` | 200 | 404 |
| POST | `/categorias` | 201 + Location | 400 · 409 |
| PUT | `/categorias/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/categorias/{id}` | 204 | 404 · 409 |
| GET | `/moradores` | 200 | — |
| GET | `/moradores/{id}` | 200 | 404 |
| POST | `/moradores` | 201 + Location | 400 · 409 |
| POST | `/moradores/login` | 200 | 400 · 401 |
| PUT | `/moradores/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/moradores/{id}` | 204 | 404 · 409 |
| GET | `/ocorrencias?status=&categoriaId=` | 200 | 400 |
| GET | `/ocorrencias/{id}` | 200 | 404 |
| POST | `/ocorrencias` | 201 + Location | 400 · 409 |
| PUT | `/ocorrencias/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/ocorrencias/{id}` | 204 | 404 · 409 |
| GET | `/ocorrencias/{id}/atualizacoes` | 200 | 404 |
| POST | `/ocorrencias/{id}/atualizacoes` | 201 + Location | 400 · 404 · 409 |
| GET | `/ocorrencias/{id}/confirmacoes` | 200 | 404 |
| POST | `/ocorrencias/{id}/confirmacoes` | 201 + Location | 400 · 404 · 409 |
| GET | `/atualizacoes/{id}` | 200 | 404 |
| PUT | `/atualizacoes/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/atualizacoes/{id}` | 204 | 404 · 409 |
| GET | `/confirmacoes/{id}` | 200 | 404 |
| PUT | `/confirmacoes/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/confirmacoes/{id}` | 204 | 404 |

Login com e-mail ou senha errados → **401**. Método não suportado em uma rota → **405**. Erro inesperado → **500** com mensagem genérica.

### Exemplos de JSON

**POST /ocorrencias**
```json
{
  "categoriaId": 1,
  "moradorId": 3,
  "titulo": "Buraco na frente da padaria",
  "descricao": "Buraco fundo que enche de água quando chove.",
  "latitude": -23.5935,
  "longitude": -46.6402,
  "endereco": "Rua Vergueiro, 3000",
  "urlFoto": "https://exemplo.com/foto.jpg"
}
```

**POST /moradores** (cadastro)
```json
{
  "nome": "Lucas Oliveira",
  "email": "lucas.oliveira@email.com",
  "cpf": "99351819019",
  "telefone": "11932109876",
  "subprefeitura": "Sé",
  "senha": "123456"
}
```

**POST /moradores/login**
```json
{ "email": "maria.souza@email.com", "senha": "123456" }
```
CPF validado com `@CPF` (aceita com ou sem pontuação; o banco guarda só os números) e único; telefone com DDD, só números.

Resposta 200 com os dados do morador (sem a senha). O front guarda o `id` para registrar ocorrências e confirmações.

**POST /ocorrencias/{id}/atualizacoes**
```json
{
  "status": "EM_ANALISE",
  "descricao": "Equipe de vistoria agendada.",
  "responsavel": "Subprefeitura Vila Mariana"
}
```

**POST /ocorrencias/{id}/confirmacoes**
```json
{ "moradorId": 2, "comentario": "Continua do mesmo jeito." }
```

**Erro 409 — ocorrência duplicada**
```json
{
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Já existe uma ocorrência aberta desta categoria a 30 metros (id 1). Confirme a ocorrência existente em vez de registrar outra.",
  "caminho": "/ocorrencias",
  "timestamp": "2026-11-10T14:30:00",
  "ocorrenciaExistenteId": 1
}
```

**Erro 400 — Bean Validation**
```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Existem campos inválidos na requisição.",
  "caminho": "/moradores",
  "timestamp": "2026-11-10T14:30:00",
  "campos": [ { "campo": "email", "mensagem": "Informe um e-mail válido." } ]
}
```

## Estrutura de pacotes

| Pacote | Responsabilidade |
|--------|------------------|
| `model` | Entidades do domínio e o enum `StatusOcorrencia` (com as regras do ciclo de vida) |
| `dao` | Acesso ao Oracle com JDBC (`DataSource` no construtor + `PreparedStatement`) |
| `dto` | Entrada (com Bean Validation) e saída da API, e o JSON de erro padronizado |
| `service` | Regras de negócio e conversão com ModelMapper |
| `controller` | Rotas REST e status HTTP |
| `exception` | Exceções próprias do domínio |
| `handler` | `@RestControllerAdvice` que transforma toda exceção em JSON |
| `config` | CORS e o bean do ModelMapper |

## Como rodar localmente

1. Execute o script `database/script_cidade_em_dia.sql` no Oracle da FIAP (SQL Developer).
2. Defina as variáveis de ambiente (no IntelliJ: *Run > Edit Configurations > Environment variables*):

   | Variável | Obrigatória | Descrição |
   |----------|-------------|-----------|
   | `DB_USER` | sim | usuário do Oracle (ex.: rm123456) |
   | `DB_PASSWORD` | sim | senha do Oracle |
   | `DB_URL` | não | padrão `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
   | `CORS_ORIGINS` | não | origens liberadas, separadas por vírgula (padrão `*`) |
   | `PORT` | não | porta HTTP (padrão 8080) |

3. Rode a classe `CidadeEmDiaApplication` ou `./mvnw spring-boot:run`.
4. Acesse `http://localhost:8080/`.

## Deploy (Render)

1. Suba o projeto para um repositório no GitHub.
2. No Render: *New > Web Service*, escolha o repositório e o ambiente **Docker** (usa o `Dockerfile` multi-stage).
3. Em *Environment*, cadastre `DB_USER` e `DB_PASSWORD` (os valores **não** vão para o código).
4. Após o deploy, teste `https://SEU-SERVICO.onrender.com/`.

> O plano gratuito do Render "dorme" sem uso. Acesse a URL alguns minutos antes da apresentação.

## Uso de IA generativa

Claude (Anthropic) foi usado como apoio na estruturação do projeto, na revisão do código e na explicação dos conceitos.
Todo o código foi revisado e entendido pelo grupo.
