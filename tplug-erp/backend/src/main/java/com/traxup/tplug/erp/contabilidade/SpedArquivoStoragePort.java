package com.traxup.tplug.erp.contabilidade;

public interface SpedArquivoStoragePort {
    void armazenar(String chave, byte[] conteudo, String hashSha256);
}
