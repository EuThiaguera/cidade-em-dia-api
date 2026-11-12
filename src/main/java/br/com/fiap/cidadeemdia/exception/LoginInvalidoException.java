package br.com.fiap.cidadeemdia.exception;

/** E-mail não cadastrado ou senha errada. Vira HTTP 401 (Unauthorized). */
public class LoginInvalidoException extends RuntimeException {

    public LoginInvalidoException() {
        super("E-mail ou senha inválidos.");
    }
}
