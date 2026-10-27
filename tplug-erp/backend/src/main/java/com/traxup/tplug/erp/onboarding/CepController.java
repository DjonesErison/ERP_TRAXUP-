package com.traxup.tplug.erp.onboarding;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cep")
public class CepController {
    private final RestClient viaCep = RestClient.create("https://viacep.com.br");

    @GetMapping("/{cep}")
    public Map<?, ?> buscar(@PathVariable String cep) {
        String digitos = cep.replaceAll("\\D", "");
        if (!digitos.matches("[0-9]{8}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP deve conter 8 dígitos");
        }
        try {
            Map<?, ?> endereco = viaCep.get().uri("/ws/{cep}/json/", digitos).retrieve().body(Map.class);
            if (endereco == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Consulta de CEP sem resposta");
            return endereco;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Serviço de CEP indisponível", exception);
        }
    }
}
