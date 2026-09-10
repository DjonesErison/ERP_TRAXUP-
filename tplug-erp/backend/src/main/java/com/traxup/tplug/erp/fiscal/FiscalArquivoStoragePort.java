package com.traxup.tplug.erp.fiscal;

public interface FiscalArquivoStoragePort {
    void armazenar(String chave, byte[] conteudo, String hashSha256);
    byte[] baixar(String chave);
}
