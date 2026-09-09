import {Component} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

type Item = {title:string; detail:string; status?:string};
type PageData = {subtitle:string; summary:string; items:Item[]};

@Component({
  standalone:true,
  template:`<section class="page">
    <div class="page-head"><div><h1>{{title}}</h1><p>{{page.subtitle}}</p></div></div>
    <div class="card panel">
      <div class="panel-title"><h2>{{icon}} {{title}}</h2><span class="badge">Conteúdo consolidado</span></div>
      <p>{{page.summary}}</p>
      <div class="module-grid">
        @for(i of page.items; track i.title){
          <div class="card module-card">
            <div><h3>{{i.title}}</h3><p>{{i.detail}}</p>@if(i.status){<small>{{i.status}}</small>}</div>
          </div>
        }
      </div>
    </div>
  </section>`
})
export class SimplePageComponent {
  title=''; icon='□'; page:PageData={subtitle:'',summary:'',items:[]};
  private pages:Record<string,PageData>={
    'Regras de Negócio':{
      subtitle:'Requisitos oficiais e decisões funcionais do TRAXUP.',
      summary:'Esta seção separa regra definida de funcionalidade já implementada. Estar documentado aqui não significa estar concluído no código.',
      items:[
        {title:'Multi-tenant',detail:'Isolamento por empresa/tenant em toda a plataforma, incluindo autenticação, dados e permissões.',status:'Regra oficial'},
        {title:'Fiscal isolado',detail:'NF-e/NFC-e, contingência, rejeições, homologação e armazenamento fiscal ficam desacoplados dos demais módulos.',status:'Regra oficial'},
        {title:'PDV offline',detail:'Cada terminal possui SQLite local, série própria, sincronização posterior e operação sem conexão.',status:'Regra oficial'},
        {title:'XML por 5 anos',detail:'Repositório fiscal por cliente com acesso da contabilidade, SPED, inventário e documentos fiscais.',status:'Regra oficial'},
        {title:'Produto Combo',detail:'Combos fixos ou com escolhas, adicionais, promoções, validade e baixa por componentes.',status:'Especificado'},
        {title:'Conciliação de Cartões',detail:'Cruzar vendas, recebimentos, taxas, antecipações, estornos e divergências com adquirentes/PSPs.',status:'Especificado'},
        {title:'Segunda Tela do Cliente',detail:'Itens, total, CPF, PIX, ofertas, avaliação e operação offline no PDV.',status:'Especificado'},
        {title:'Trial e White Label',detail:'Teste por 7 dias, ERP liberado e PDV demo com produtos de demonstração.',status:'Especificado'}
      ]
    },
    'Referências / Imagens':{
      subtitle:'Materiais visuais e referências usados para decisões de produto.',
      summary:'Catálogo de referências do projeto. Nenhuma imagem de terceiros é tratada como implementação concluída.',
      items:[
        {title:'PDV Desktop',detail:'Referência visual para tela principal de venda rápida, busca de produtos, totais e pagamentos.'},
        {title:'Segunda Tela PDV',detail:'Referência para experiência do cliente com carrinho, QR PIX, ofertas e avaliação.'},
        {title:'Central do Produto',detail:'Referência visual e funcional para acompanhamento do roadmap, módulos, testes e histórico.'},
        {title:'Painel SaaS Admin',detail:'Referência para clientes, planos, faturamento, inadimplência, bloqueios e certificados.'},
        {title:'Inventário Mobile',detail:'Fluxo de contagem, divergência, aprovação e ajuste pelo celular.'}
      ]
    },
    'Dependências':{
      subtitle:'Stack técnica e serviços essenciais do ecossistema.',
      summary:'Dependências registradas conforme a arquitetura atual do TRAXUP; versões exatas devem ser confirmadas no repositório de cada componente.',
      items:[
        {title:'Angular 19',detail:'Frontend web da Central e base prevista para aplicações web do ERP.',status:'Em uso'},
        {title:'Java + Spring Boot',detail:'Backend principal e APIs REST.',status:'Em uso'},
        {title:'PostgreSQL',detail:'Banco principal multi-tenant na nuvem.',status:'Em uso'},
        {title:'SQLite',detail:'Banco local por terminal do PDV para operação offline.',status:'Arquitetura definida'},
        {title:'Kafka + Redis',detail:'Mensageria/eventos e apoio a filas/cache.',status:'Arquitetura definida'},
        {title:'Docker',detail:'Empacotamento e implantação dos serviços.',status:'Em uso'},
        {title:'S3 compatível',detail:'Armazenamento de XML, DANFE e arquivos fiscais.',status:'Arquitetura definida'},
        {title:'JWT + Refresh Token',detail:'Autenticação e renovação de sessão.',status:'Arquitetura definida'}
      ]
    },
    'Testes':{
      subtitle:'Visão operacional da qualidade e validações automatizadas.',
      summary:'Snapshot operacional atualizado após o saneamento da Central. CI da Central e testes do backend são acompanhados separadamente.',
      items:[
        {title:'Central Angular — CI',detail:'Workflow TRAXUP Central - CI executado na main para o commit f1fef621 do saneamento completo.',status:'SUCESSO — 09/09/2026'},
        {title:'Central Angular — Docker',detail:'Workflow TRAXUP Central - Docker construiu e publicou a imagem correspondente ao commit f1fef621.',status:'SUCESSO — 09/09/2026'},
        {title:'Produção — origem local',detail:'Container traxup-central respondeu HTTP 200 em 127.0.0.1:8081 após a atualização.',status:'VALIDADO'},
        {title:'Produção — domínio público',detail:'central.traxup.com.br respondeu HTTP/2 200 através de Caddy + Nginx após o deploy.',status:'VALIDADO'},
        {title:'Backend — testes automatizados',detail:'Último marco consolidado do backend registrava 375 testes, migrations e Docker verdes.',status:'Último marco conhecido: verde'},
        {title:'Migrations',detail:'Validação obrigatória no CI do backend antes de aceitar merge.',status:'Obrigatória'},
        {title:'Fiscal',detail:'Perfil Fiscal por Filial permanece como marco implementado; cobertura deve acompanhar a evolução de NF-e/NFC-e.',status:'Em evolução'},
        {title:'PDV offline',detail:'Sincronização, conflitos, séries por terminal e recuperação de conexão exigirão suíte dedicada.',status:'Planejado'}
      ]
    },
    'Histórico':{
      subtitle:'Marcos reais e decisões relevantes da implementação.',
      summary:'Linha do tempo resumida dos principais marcos consolidados do TRAXUP.',
      items:[
        {title:'Central Angular 19 criada',detail:'Primeira versão visual da ERP Central do Produto, com navegação e páginas-base.',status:'Concluído'},
        {title:'Container traxup-central',detail:'Frontend publicado em Docker/Nginx e distribuído pelo GitHub Container Registry.',status:'Concluído'},
        {title:'Domínio oficial',detail:'central.traxup.com.br configurado com Caddy, HTTPS e reverse proxy para a Central.',status:'Concluído'},
        {title:'Perfil Fiscal por Filial',detail:'Marco backend com isolamento por tenant, validação fiscal, RBAC, auditoria e persistência.',status:'Concluído'},
        {title:'PR #212',detail:'Dashboard, módulos, funcionalidades e roadmap foram saneados para retirar dados fictícios.',status:'Mergeada'},
        {title:'PR #214',detail:'Regras, referências, dependências, testes, histórico, GitHub, configurações, ideias e documentação receberam conteúdo consolidado.',status:'Mergeada em 09/09/2026'},
        {title:'Deploy da Central saneada',detail:'CI e Docker verdes; nova imagem publicada no GHCR e container de produção recriado com validação HTTP 200 interna e externa.',status:'Produção validada — 09/09/2026'},
        {title:'Próxima evolução da Central',detail:'Automatizar snapshots de GitHub/CI e reduzir etapas manuais de implantação.',status:'Em andamento'}
      ]
    },
    'GitHub':{
      subtitle:'Repositórios, CI/CD e situação de integração.',
      summary:'Snapshot operacional confirmado em 09/09/2026. Dados históricos são registrados sem apresentar integração automática como pronta.',
      items:[
        {title:'ERP_TRAXUP-',detail:'Repositório privado da Central TRAXUP em Angular.',status:'Ativo'},
        {title:'Branch main',detail:'Produção da Central atualmente no commit f1fef621 após merge da PR #214.',status:'Atualizada'},
        {title:'PR #212',detail:'Atualização consolidada de Dashboard, módulos, funcionalidades e roadmap.',status:'Mergeada'},
        {title:'PR #214',detail:'Saneamento das telas restantes da Central.',status:'Mergeada'},
        {title:'TRAXUP Central - CI',detail:'Run pós-merge do commit f1fef621 concluída sem erro.',status:'SUCCESS'},
        {title:'TRAXUP Central - Docker',detail:'Run pós-merge do commit f1fef621 concluída sem erro e imagem latest publicada.',status:'SUCCESS'},
        {title:'Imagem de produção',detail:'ghcr.io/djoneserison/traxup-central:latest — digest implantado: sha256:bcf7bd2e6041f5ec21ae1c8702144fabe44e99741fcaf8073ddd22a371d1b25e.',status:'Em produção'},
        {title:'Deploy VPS',detail:'Pull e recriação do container ainda são manuais; automação segura de deploy é a próxima melhoria de infraestrutura.',status:'Parcial'}
      ]
    },
    'Configurações':{
      subtitle:'Ambientes e parâmetros públicos de infraestrutura.',
      summary:'Somente informações não sensíveis são exibidas aqui. Tokens, senhas, chaves e segredos nunca devem aparecer na Central.',
      items:[
        {title:'Central',detail:'https://central.traxup.com.br',status:'Produção — HTTP 200 validado'},
        {title:'Container',detail:'ghcr.io/djoneserison/traxup-central:latest',status:'Ativo'},
        {title:'Imagem implantada',detail:'Digest sha256:bcf7bd2e6041f5ec21ae1c8702144fabe44e99741fcaf8073ddd22a371d1b25e.',status:'Validada em 09/09/2026'},
        {title:'Porta local',detail:'127.0.0.1:8081 → 80 no container da Central.',status:'HTTP 200 validado'},
        {title:'Proxy',detail:'Caddy com HTTPS e reverse proxy para Nginx da Central.',status:'Ativo'},
        {title:'Diretório padrão',detail:'/opt/traxup para arquivos específicos de implantação e configuração do TRAXUP.',status:'Regra de infraestrutura'},
        {title:'Segredos',detail:'Devem permanecer apenas em mecanismos seguros de ambiente/CI e nunca no frontend.',status:'Protegidos'}
      ]
    }
  };
  constructor(route:ActivatedRoute){
    this.title=route.snapshot.data['title']??'';
    this.icon=route.snapshot.data['icon']??'□';
    this.page=this.pages[this.title]??{subtitle:'Conteúdo em consolidação.',summary:'Esta área ainda não possui dados consolidados.',items:[]};
  }
}
