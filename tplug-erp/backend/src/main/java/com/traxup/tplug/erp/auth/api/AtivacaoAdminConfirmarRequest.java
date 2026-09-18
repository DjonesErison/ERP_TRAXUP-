package com.traxup.tplug.erp.auth.api;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AtivacaoAdminConfirmarRequest(@NotBlank String token,@NotBlank @Size(min=8,max=72) String novaSenha){}
