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
      summary:'O status abaixo registra evidências conhecidas do projeto e diferencia CI da Central dos testes do backend.',
      items:[
        {title:'Central Angular — CI',detail:'Pipeline executa instalação de dependências e build Angular a cada PR/push relevante.',status:'Verde no último merge da Central'},
        {title:'Central Angular — Docker',detail:'Pipeline publica ghcr.io/djoneserison/traxup-central:latest após push na main.',status:'Verde no último deploy'},
        {title:'Backend — testes automatizados',detail:'Último marco conhecido incluía suíte de testes e validação de migrations antes do Docker.',status:'Último marco consolidado: verde'},
        {title:'Migrations',detail:'Validação obrigatória no CI do backend antes de aceitar merge.',status:'Obrigatória'},
        {title:'Fiscal',detail:'Cenários de Perfil Fiscal por filial e evolução de NF-e/NFC-e devem permanecer cobertos por testes.',status:'Em evolução'},
        {title:'PDV offline',detail:'Testes de sincronização, conflito, séries por terminal e recuperação de conexão ainda precisam ser ampliados.',status:'Planejado'}
      ]
    },
    'Histórico':{
      subtitle:'Marcos reais e decisões relevantes da implementação.',
      summary:'Linha do tempo resumida dos principais marcos consolidados do TRAXUP.',
      items:[
        {title:'Central Angular 19 criada',detail:'Primeira versão visual da ERP Central do Produto, com navegação e páginas-base.',status:'Concluído'},
        {title:'Container traxup-central',detail:'Frontend publicado em Docker/Nginx e distribuído pelo GitHub Container Registry.',status:'Concluído'},
        {title:'Domínio oficial',detail:'central.traxup.com.br configurado com DNS, Caddy, HTTPS e reverse proxy para a Central.',status:'Concluído'},
        {title:'Perfil Fiscal por Filial',detail:'Último marco backend consolidado antes desta rodada de atualização da Central.',status:'Concluído'},
        {title:'PR #212',detail:'Dashboard, módulos, funcionalidades e roadmap foram saneados para retirar dados fictícios.',status:'Mergeada'},
        {title:'Saneamento completo da Central',detail:'Substituição dos placeholders de regras, referências, dependências, testes, histórico e GitHub por conteúdo real.',status:'Em implementação'}
      ]
    },
    'GitHub':{
      subtitle:'Repositórios, CI/CD e situação de integração.',
      summary:'Visão resumida do repositório da Central e dos processos de entrega atualmente confirmados.',
      items:[
        {title:'ERP_TRAXUP-',detail:'Repositório privado da ERP Central do Produto em Angular.',status:'Ativo'},
        {title:'Branch main',detail:'Branch de produção da Central; merges nela disparam CI e publicação Docker.',status:'Ativa'},
        {title:'TRAXUP Central - CI',detail:'Valida build Angular em pull requests e push.',status:'Ativo'},
        {title:'TRAXUP Central - Docker',detail:'Gera e publica a imagem traxup-central:latest no GHCR.',status:'Ativo'},
        {title:'PR #212',detail:'Atualização consolidada de status do produto.',status:'Mergeada'},
        {title:'Deploy VPS',detail:'Atualização da imagem ainda é executada manualmente na VPS; automação é próxima melhoria de infraestrutura.',status:'Parcial'}
      ]
    },
    'Configurações':{
      subtitle:'Ambientes e parâmetros públicos de infraestrutura.',
      summary:'Somente informações não sensíveis são exibidas aqui. Tokens, senhas, chaves e segredos nunca devem aparecer na Central.',
      items:[
        {title:'Central',detail:'https://central.traxup.com.br',status:'Produção'},
        {title:'Container',detail:'ghcr.io/djoneserison/traxup-central:latest',status:'Ativo'},
        {title:'Porta local',detail:'127.0.0.1:8081 → 80 no container da Central.',status:'Ativa'},
        {title:'Proxy',detail:'Caddy com HTTPS automático e reverse proxy para a Central.',status:'Ativo'},
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
