-- =====================================================================
--  CIDADE EM DIA - Script de criação do banco (Oracle FIAP)
--  Global Solution 2026/2 - Domain Driven Design Using Java
--
--  Padrão da disciplina: tabelas tb_nome, colunas cd_/nm_/ds_/dt_/nr_
--  e sequences sq_tb_nome.
--
--  Ordem: sequences -> tabelas (na ordem das FKs) -> comentários -> dados
-- =====================================================================

-- ---------------------------------------------------------------------
--  (OPCIONAL) Para recriar tudo do zero, descomente os DROPs abaixo.
--  ATENÇÃO: isso apaga as tabelas e os dados que já existirem.
-- ---------------------------------------------------------------------
-- DROP TABLE tb_confirmacao CASCADE CONSTRAINTS;
-- DROP TABLE tb_atualizacao CASCADE CONSTRAINTS;
-- DROP TABLE tb_ocorrencia  CASCADE CONSTRAINTS;
-- DROP TABLE tb_morador     CASCADE CONSTRAINTS;
-- DROP TABLE tb_categoria   CASCADE CONSTRAINTS;
-- DROP SEQUENCE sq_tb_confirmacao;
-- DROP SEQUENCE sq_tb_atualizacao;
-- DROP SEQUENCE sq_tb_ocorrencia;
-- DROP SEQUENCE sq_tb_morador;
-- DROP SEQUENCE sq_tb_categoria;

-- ---------------------------------------------------------------------
--  SEQUENCES
-- ---------------------------------------------------------------------
CREATE SEQUENCE sq_tb_categoria   START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_morador     START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_ocorrencia  START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_atualizacao START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_confirmacao START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
--  TABELAS
-- ---------------------------------------------------------------------
CREATE TABLE tb_categoria (
    cd_categoria  NUMBER(10)    NOT NULL,
    nm_categoria  VARCHAR2(60)  NOT NULL,
    ds_categoria  VARCHAR2(255),
    CONSTRAINT pk_tb_categoria PRIMARY KEY (cd_categoria),
    CONSTRAINT uk_tb_categoria_nome UNIQUE (nm_categoria)
);

CREATE TABLE tb_morador (
    cd_morador   NUMBER(10)    NOT NULL,
    nm_morador   VARCHAR2(100) NOT NULL,
    ds_email     VARCHAR2(120) NOT NULL,
    nr_cpf       VARCHAR2(11)  NOT NULL,
    nr_telefone  VARCHAR2(11)  NOT NULL,
    nm_subprefeitura VARCHAR2(80) NOT NULL,
    ds_senha     VARCHAR2(60)  NOT NULL,
    dt_cadastro  TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_tb_morador PRIMARY KEY (cd_morador),
    CONSTRAINT uk_tb_morador_email UNIQUE (ds_email),
    CONSTRAINT uk_tb_morador_cpf UNIQUE (nr_cpf),
    CONSTRAINT ck_morador_cpf CHECK (LENGTH(nr_cpf) = 11)
);

CREATE TABLE tb_ocorrencia (
    cd_ocorrencia  NUMBER(10)    NOT NULL,
    cd_categoria   NUMBER(10)    NOT NULL,
    cd_morador     NUMBER(10)    NOT NULL,
    ds_titulo      VARCHAR2(100) NOT NULL,
    ds_ocorrencia  VARCHAR2(500) NOT NULL,
    nr_latitude    NUMBER(9,6)   NOT NULL,
    nr_longitude   NUMBER(9,6)   NOT NULL,
    ds_endereco    VARCHAR2(150),
    ds_url_foto    VARCHAR2(255),
    ds_status      VARCHAR2(20)  DEFAULT 'REGISTRADA' NOT NULL,
    dt_abertura    TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_tb_ocorrencia PRIMARY KEY (cd_ocorrencia),
    CONSTRAINT fk_ocorrencia_categoria FOREIGN KEY (cd_categoria) REFERENCES tb_categoria (cd_categoria),
    CONSTRAINT fk_ocorrencia_morador   FOREIGN KEY (cd_morador)   REFERENCES tb_morador (cd_morador),
    CONSTRAINT ck_ocorrencia_status CHECK (ds_status IN ('REGISTRADA','EM_ANALISE','EM_EXECUCAO','CONCLUIDA','DESCARTADA')),
    CONSTRAINT ck_ocorrencia_latitude  CHECK (nr_latitude  BETWEEN -90  AND 90),
    CONSTRAINT ck_ocorrencia_longitude CHECK (nr_longitude BETWEEN -180 AND 180)
);

CREATE TABLE tb_atualizacao (
    cd_atualizacao      NUMBER(10)    NOT NULL,
    cd_ocorrencia       NUMBER(10)    NOT NULL,
    ds_status_anterior  VARCHAR2(20)  NOT NULL,
    ds_status_novo      VARCHAR2(20)  NOT NULL,
    ds_atualizacao      VARCHAR2(500) NOT NULL,
    nm_responsavel      VARCHAR2(100) NOT NULL,
    ds_url_evidencia    VARCHAR2(255),
    dt_atualizacao      TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_tb_atualizacao PRIMARY KEY (cd_atualizacao),
    CONSTRAINT fk_atualizacao_ocorrencia FOREIGN KEY (cd_ocorrencia) REFERENCES tb_ocorrencia (cd_ocorrencia),
    CONSTRAINT ck_atualizacao_status_novo CHECK (ds_status_novo IN ('REGISTRADA','EM_ANALISE','EM_EXECUCAO','CONCLUIDA','DESCARTADA'))
);

CREATE TABLE tb_confirmacao (
    cd_confirmacao  NUMBER(10)    NOT NULL,
    cd_ocorrencia   NUMBER(10)    NOT NULL,
    cd_morador      NUMBER(10)    NOT NULL,
    ds_comentario   VARCHAR2(255),
    dt_confirmacao  TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_tb_confirmacao PRIMARY KEY (cd_confirmacao),
    CONSTRAINT fk_confirmacao_ocorrencia FOREIGN KEY (cd_ocorrencia) REFERENCES tb_ocorrencia (cd_ocorrencia),
    CONSTRAINT fk_confirmacao_morador    FOREIGN KEY (cd_morador)    REFERENCES tb_morador (cd_morador),
    CONSTRAINT uk_confirmacao_morador UNIQUE (cd_ocorrencia, cd_morador)
);

-- ---------------------------------------------------------------------
--  COMENTÁRIOS
-- ---------------------------------------------------------------------
COMMENT ON TABLE tb_categoria   IS 'Tipos de problema urbano (buraco, alagamento, iluminação...)';
COMMENT ON TABLE tb_morador     IS 'Cidadãos que registram e confirmam ocorrências';
COMMENT ON COLUMN tb_morador.nm_subprefeitura IS 'Subprefeitura responsável pela região onde o morador vive';
COMMENT ON TABLE tb_ocorrencia  IS 'Problema urbano registrado por um morador em um ponto do mapa';
COMMENT ON TABLE tb_atualizacao IS 'Linha do tempo da ocorrência: cada mudança de status feita pela gestão';
COMMENT ON TABLE tb_confirmacao IS 'Confirmação de outro morador de que o problema continua existindo';

-- ---------------------------------------------------------------------
--  DADOS DE EXEMPLO (fictícios, para demonstração)
-- ---------------------------------------------------------------------
INSERT INTO tb_categoria (cd_categoria, nm_categoria, ds_categoria) VALUES (sq_tb_categoria.NEXTVAL, 'Buraco', 'Buracos e afundamentos no asfalto');
INSERT INTO tb_categoria (cd_categoria, nm_categoria, ds_categoria) VALUES (sq_tb_categoria.NEXTVAL, 'Alagamento', 'Pontos que acumulam água com a chuva');
INSERT INTO tb_categoria (cd_categoria, nm_categoria, ds_categoria) VALUES (sq_tb_categoria.NEXTVAL, 'Iluminação', 'Postes apagados ou iluminação insuficiente');
INSERT INTO tb_categoria (cd_categoria, nm_categoria, ds_categoria) VALUES (sq_tb_categoria.NEXTVAL, 'Lixo', 'Descarte irregular de lixo e entulho');
INSERT INTO tb_categoria (cd_categoria, nm_categoria, ds_categoria) VALUES (sq_tb_categoria.NEXTVAL, 'Acessibilidade', 'Calçadas danificadas, falta de rampas e obstáculos');
INSERT INTO tb_categoria (cd_categoria, nm_categoria, ds_categoria) VALUES (sq_tb_categoria.NEXTVAL, 'Arborização', 'Árvores caídas, poda ou falta de arborização');

INSERT INTO tb_morador (cd_morador, nm_morador, ds_email, nr_cpf, nr_telefone, nm_subprefeitura, ds_senha) VALUES (sq_tb_morador.NEXTVAL, 'Maria Souza', 'maria.souza@email.com', '52601815906', '11987654321', 'Vila Mariana', '123456');
INSERT INTO tb_morador (cd_morador, nm_morador, ds_email, nr_cpf, nr_telefone, nm_subprefeitura, ds_senha) VALUES (sq_tb_morador.NEXTVAL, 'João Lima', 'joao.lima@email.com', '08301661305', '11976543210', 'Vila Mariana', '123456');
INSERT INTO tb_morador (cd_morador, nm_morador, ds_email, nr_cpf, nr_telefone, nm_subprefeitura, ds_senha) VALUES (sq_tb_morador.NEXTVAL, 'Ana Ferreira', 'ana.ferreira@email.com', '18609139034', '11965432109', 'Vila Mariana', '123456');
INSERT INTO tb_morador (cd_morador, nm_morador, ds_email, nr_cpf, nr_telefone, nm_subprefeitura, ds_senha) VALUES (sq_tb_morador.NEXTVAL, 'Carlos Pereira', 'carlos.pereira@email.com', '99603082430', '11954321098', 'Vila Mariana', '123456');
INSERT INTO tb_morador (cd_morador, nm_morador, ds_email, nr_cpf, nr_telefone, nm_subprefeitura, ds_senha) VALUES (sq_tb_morador.NEXTVAL, 'Beatriz Santos', 'beatriz.santos@email.com', '62819482112', '11943210987', 'Ipiranga', '123456');

INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_categoria, cd_morador, ds_titulo, ds_ocorrencia, nr_latitude, nr_longitude, ds_endereco, ds_status)
VALUES (sq_tb_ocorrencia.NEXTVAL, 1, 1, 'Buraco grande na faixa da direita', 'Buraco com cerca de um metro, carros desviam para a contramão.', -23.588900, -46.634100, 'Rua Domingos de Morais, 1200', 'EM_ANALISE');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_categoria, cd_morador, ds_titulo, ds_ocorrencia, nr_latitude, nr_longitude, ds_endereco, ds_status)
VALUES (sq_tb_ocorrencia.NEXTVAL, 2, 3, 'Rua alaga toda vez que chove', 'A água sobe até a calçada e invade o ponto de ônibus.', -23.601200, -46.665800, 'Av. Ibirapuera, 2300', 'REGISTRADA');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_categoria, cd_morador, ds_titulo, ds_ocorrencia, nr_latitude, nr_longitude, ds_endereco, ds_status)
VALUES (sq_tb_ocorrencia.NEXTVAL, 3, 4, 'Três postes apagados', 'Trecho escuro perto da escola, moradores evitam passar à noite.', -23.614500, -46.639900, 'Rua Carneiro da Cunha, 450', 'EM_EXECUCAO');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_categoria, cd_morador, ds_titulo, ds_ocorrencia, nr_latitude, nr_longitude, ds_endereco, ds_status)
VALUES (sq_tb_ocorrencia.NEXTVAL, 5, 5, 'Calçada sem rampa na esquina', 'Cadeirantes precisam descer para a rua para atravessar.', -23.592300, -46.608700, 'Rua Bom Pastor, 80', 'REGISTRADA');
INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_categoria, cd_morador, ds_titulo, ds_ocorrencia, nr_latitude, nr_longitude, ds_endereco, ds_status)
VALUES (sq_tb_ocorrencia.NEXTVAL, 4, 2, 'Entulho descartado na praça', 'Restos de obra jogados ao lado do parquinho.', -23.586700, -46.636200, 'Praça Dr. Carvalho Franco', 'CONCLUIDA');

INSERT INTO tb_atualizacao (cd_atualizacao, cd_ocorrencia, ds_status_anterior, ds_status_novo, ds_atualizacao, nm_responsavel)
VALUES (sq_tb_atualizacao.NEXTVAL, 1, 'REGISTRADA', 'EM_ANALISE', 'Equipe de vistoria agendada para avaliar o buraco.', 'Subprefeitura Vila Mariana');
INSERT INTO tb_atualizacao (cd_atualizacao, cd_ocorrencia, ds_status_anterior, ds_status_novo, ds_atualizacao, nm_responsavel)
VALUES (sq_tb_atualizacao.NEXTVAL, 3, 'REGISTRADA', 'EM_ANALISE', 'Chamado aberto com a equipe de iluminação pública.', 'Departamento de Iluminação');
INSERT INTO tb_atualizacao (cd_atualizacao, cd_ocorrencia, ds_status_anterior, ds_status_novo, ds_atualizacao, nm_responsavel)
VALUES (sq_tb_atualizacao.NEXTVAL, 3, 'EM_ANALISE', 'EM_EXECUCAO', 'Troca das lâmpadas programada para esta semana.', 'Departamento de Iluminação');
INSERT INTO tb_atualizacao (cd_atualizacao, cd_ocorrencia, ds_status_anterior, ds_status_novo, ds_atualizacao, nm_responsavel)
VALUES (sq_tb_atualizacao.NEXTVAL, 5, 'REGISTRADA', 'EM_ANALISE', 'Local vistoriado, remoção solicitada.', 'Subprefeitura Vila Mariana');
INSERT INTO tb_atualizacao (cd_atualizacao, cd_ocorrencia, ds_status_anterior, ds_status_novo, ds_atualizacao, nm_responsavel)
VALUES (sq_tb_atualizacao.NEXTVAL, 5, 'EM_ANALISE', 'EM_EXECUCAO', 'Caminhão de coleta a caminho.', 'Subprefeitura Vila Mariana');
INSERT INTO tb_atualizacao (cd_atualizacao, cd_ocorrencia, ds_status_anterior, ds_status_novo, ds_atualizacao, nm_responsavel, ds_url_evidencia)
VALUES (sq_tb_atualizacao.NEXTVAL, 5, 'EM_EXECUCAO', 'CONCLUIDA', 'Entulho removido e área limpa.', 'Subprefeitura Vila Mariana', 'https://exemplo.com/fotos/praca-depois.jpg');

INSERT INTO tb_confirmacao (cd_confirmacao, cd_ocorrencia, cd_morador, ds_comentario) VALUES (sq_tb_confirmacao.NEXTVAL, 1, 2, 'Passo por aqui todo dia, continua igual.');
INSERT INTO tb_confirmacao (cd_confirmacao, cd_ocorrencia, cd_morador, ds_comentario) VALUES (sq_tb_confirmacao.NEXTVAL, 1, 4, 'Meu carro quase caiu nele ontem.');
INSERT INTO tb_confirmacao (cd_confirmacao, cd_ocorrencia, cd_morador, ds_comentario) VALUES (sq_tb_confirmacao.NEXTVAL, 2, 1, 'Alagou de novo na última chuva.');
INSERT INTO tb_confirmacao (cd_confirmacao, cd_ocorrencia, cd_morador, ds_comentario) VALUES (sq_tb_confirmacao.NEXTVAL, 2, 5, NULL);
INSERT INTO tb_confirmacao (cd_confirmacao, cd_ocorrencia, cd_morador, ds_comentario) VALUES (sq_tb_confirmacao.NEXTVAL, 4, 3, 'Minha mãe usa cadeira de rodas e não consegue passar.');

COMMIT;
