# Etapa 0 — Inventário e limpeza de branches

Data da auditoria: 17/09/2026.

## Resultado do inventário

O repositório possuía 365 branches no levantamento completo. O volume é resultado de branches de implementação incremental, correções, testes, documentação, tentativas de promoção e branches temporárias acumuladas.

## Branches permanentes

Preservar sempre:

- `main`
- `develop`
- `homologacao`

## Exclusão comprovadamente segura — lote inicial

As branches abaixo foram comparadas contra `homologacao` e possuem `ahead_by = 0`, portanto não carregam commits exclusivos em relação à linha publicada atual:

- `promote/homologacao-etapa1-clean`
- `release/etapa1-ui-aprovada`
- `fix/etapa1-fluxos-aprovados`

Também podem ser removidas após confirmação do merge deste documento as próprias branches documentais temporárias já mergeadas.

## Obsoleta — não reutilizar

- `promote/develop-homologacao-etapa1`: branch do PR #334, encerrado como substituído. A comparação mostra histórico divergente e remoções de arquivos de homologação; não deve ser promovida nem usada como base. Deve ser excluída após preservação deste registro.

## Temporárias evidentes — revisar e remover

- `tmp/rebase-pr151-20260908`
- `tmp-noop-check`
- `tmp-should-not-exist-2`

Essas branches têm nomenclatura explicitamente temporária. Antes da exclusão física em lote, executar a checagem de commits exclusivos descrita abaixo.

## Regra para o restante das branches

Não excluir uma branch apenas pelo nome. Para cada branch restante:

1. identificar a branch permanente de destino (`develop`, `homologacao` ou `main`);
2. executar `git rev-list --count <destino>..<branch>`;
3. se o resultado for `0` e não houver PR aberto válido, a branch pode ser removida;
4. se houver commits exclusivos, preservar para revisão ou consolidar em PR antes de remover;
5. PR mergeado não exige preservação da branch: o histórico continua no GitHub.

## Script seguro para limpeza local/remota

Executar a partir de um clone atualizado e autenticado. O script abaixo nunca remove `main`, `develop` ou `homologacao`; ele remove apenas branches remotas que não possuem commits exclusivos contra nenhuma das três linhas permanentes.

```bash
set -euo pipefail

git fetch --all --prune

for ref in $(git for-each-ref --format='%(refname:short)' refs/remotes/origin \
  | sed 's#^origin/##' \
  | grep -v '^HEAD$' \
  | grep -v -E '^(main|develop|homologacao)$'); do

  ahead_main=$(git rev-list --count origin/main..origin/"$ref" 2>/dev/null || echo 1)
  ahead_develop=$(git rev-list --count origin/develop..origin/"$ref" 2>/dev/null || echo 1)
  ahead_homologacao=$(git rev-list --count origin/homologacao..origin/"$ref" 2>/dev/null || echo 1)

  if [ "$ahead_main" -eq 0 ] || [ "$ahead_develop" -eq 0 ] || [ "$ahead_homologacao" -eq 0 ]; then
    echo "SAFE_DELETE $ref"
  else
    echo "REVIEW      $ref main=$ahead_main develop=$ahead_develop homologacao=$ahead_homologacao"
  fi
done
```

O primeiro passe apenas imprime a classificação. Depois de revisar a saída, substituir a linha `echo "SAFE_DELETE $ref"` por `git push origin --delete "$ref"` para efetivar a limpeza.

## Política após o saneamento

- branch temporária deve ser removida depois do merge;
- um bloco funcional deve preferir uma branch e um PR;
- não criar novas branches `tmp/*` no remoto;
- branches `codex/*` antigas não são permanentes: após comprovar incorporação, remover;
- manter PRs fechados/mergeados como histórico e evidência, sem manter suas branches.
