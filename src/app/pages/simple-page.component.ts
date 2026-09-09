import {Component} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

type Item = {title:string; detail:string; status?:string};
type PageData = {subtitle:string; summary:string; items:Item[]};
type BuildInfo = {
  commit:string;
  commitShort:string;
  branch:string;
  repository:string;
  runId:string;
  runNumber:string;
  builtAt:string;
};

@Component({
  standalone:true,
  template:`<section class="page">
    <div class="page-head"><div><h1>{{title}}</h1><p>{{page.subtitle}}</p></div></div>
    @if(buildInfo && buildInfo.commit !== 'local'){
      <div class="card panel">
        <div class="panel-title"><h2>Build em produção</h2><span class="badge">Automático</span></div>
        <p>Commit {{buildInfo.commitShort}} · branch {{buildInfo.branch}} · build #{{buildInfo.runNumber}} · {{formatBuildDate(buildInfo.builtAt)}}</p>
      </div>
    }
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
  buildInfo?:BuildInfo;

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
      summary:'CI da Central, publicação Docker e marcos do backend são acompanhados separadamente. O build implantado é lido automaticamente da própria imagem em produção.',
      items:[
        {title:'Central Angular — CI',detail:'Validação de teste e build Angular executada pelo workflow TRAXUP Central - CI em pull requests e na main.',status:'Acompanhar no GitHub Actions'},
        {title:'Central Angular — Docker',detail:'A imagem de produção publica metadados do próprio workflow Docker.',status:'Atualização automática'},
        {title:'Produção — origem local',detail:'Container traxup-central é servido em 127.0.0.1:8081 e validado pelo script de deploy.',status:'Health check automatizado'},
        {title:'Produção — domínio público',detail:'central.traxup.com.br é validado pelo script de deploy após a troca do container.',status:'Health check automatizado'},
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
        {title:'Deploy automático',detail:'Após Docker verde na main, o workflow de deploy conecta à VPS com usuário dedicado, implanta o SHA imutável e executa health checks com rollback em caso de falha.',status:'Concluído'},
        {title:'Metadados do build',detail:'A Central passa a carregar da própria imagem o SHA, branch, run e horário do build implantado.',status:'Automatizado'}
      ]
    },
    'GitHub':{
      subtitle:'Repositórios, CI/CD e situação de integração.',
      summary:'O identificador da versão em produção é obtido automaticamente da própria imagem. Nenhum token ou segredo é exposto no navegador.',
      items:[
        {title:'ERP_TRAXUP-',detail:'Repositório privado da Central TRAXUP em Angular.',status:'Ativo'},
        {title:'Branch main',detail:'A versão implantada será identificada automaticamente pelo build-info da imagem.',status:'Automático'},
        {title:'PR #212',detail:'Atualização consolidada de Dashboard, módulos, funcionalidades e roadmap.',status:'Mergeada'},
        {title:'PR #214',detail:'Saneamento das telas restantes da Central.',status:'Mergeada'},
        {title:'TRAXUP Central - CI',detail:'Workflow de teste e build Angular.',status:'Ativo'},
        {title:'TRAXUP Central - Docker',detail:'Build e publicação no GHCR com tag latest e tag imutável por SHA.',status:'Ativo'},
        {title:'Imagem de produção',detail:'ghcr.io/djoneserison/traxup-central:<SHA> — o SHA em execução é carregado automaticamente.',status:'Automático'},
        {title:'Deploy VPS',detail:'Deploy acionado automaticamente após sucesso do Docker na main, com SSH dedicado, health checks e tentativa de rollback.',status:'Automatizado'}
      ]
    },
    'Configurações':{
      subtitle:'Ambientes e parâmetros públicos de infraestrutura.',
      summary:'Somente informações não sensíveis são exibidas aqui. Tokens, senhas, chaves e segredos nunca devem aparecer na Central.',
      items:[
        {title:'Central',detail:'https://central.traxup.com.br',status:'Produção'},
        {title:'Container',detail:'ghcr.io/djoneserison/traxup-central:<SHA>',status:'Deploy por SHA imutável'},
        {title:'Imagem implantada',detail:'O SHA e os metadados do build são carregados do arquivo público build-info.json incluído na própria imagem.',status:'Automático'},
        {title:'Porta local',detail:'127.0.0.1:8081 → 80 no container da Central.',status:'Health check automatizado'},
        {title:'Proxy',detail:'Caddy com HTTPS e reverse proxy para Nginx da Central.',status:'Ativo'},
        {title:'Diretório padrão',detail:'/opt/traxup para arquivos específicos de implantação e configuração do TRAXUP.',status:'Regra de infraestrutura'},
        {title:'Segredos',detail:'Permanecem no GitHub Environment e nas credenciais locais da VPS; nunca são enviados ao frontend.',status:'Protegidos'}
      ]
    }
  };

  constructor(route:ActivatedRoute){
    this.title=route.snapshot.data['title']??'';
    this.icon=route.snapshot.data['icon']??'□';
    this.page=this.pages[this.title]??{subtitle:'Conteúdo em consolidação.',summary:'Esta área ainda não possui dados consolidados.',items:[]};
    void this.loadBuildInfo();
  }

  formatBuildDate(value:string):string {
    if (!value || value === 'local') return 'ambiente local';
    const date=new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat('pt-BR',{dateStyle:'short',timeStyle:'short',timeZone:'America/Sao_Paulo'}).format(date);
  }

  private async loadBuildInfo():Promise<void> {
    try {
      const response=await fetch('/build-info.json',{cache:'no-store'});
      if (!response.ok) return;
      const info=await response.json() as BuildInfo;
      this.buildInfo=info;
      if (!info.commit || info.commit === 'local') return;

      const builtAt=this.formatBuildDate(info.builtAt);
      this.replaceItem('GitHub','Branch main',{
        detail:`Produção executando o commit ${info.commitShort} da branch ${info.branch}.`,
        status:'Versão implantada'
      });
      this.replaceItem('GitHub','TRAXUP Central - Docker',{
        detail:`Imagem do commit ${info.commitShort} gerada pelo run Docker #${info.runNumber}.`,
        status:'SUCCESS — imagem em produção'
      });
      this.replaceItem('GitHub','Imagem de produção',{
        detail:`ghcr.io/djoneserison/traxup-central:${info.commit}`,
        status:`Build ${builtAt}`
      });
      this.replaceItem('Testes','Central Angular — Docker',{
        detail:`A versão servida foi construída no run Docker #${info.runNumber}, commit ${info.commitShort}.`,
        status:`Build ${builtAt}`
      });
      this.replaceItem('Configurações','Imagem implantada',{
        detail:`Commit ${info.commit} · build Docker #${info.runNumber}.`,
        status:`Build ${builtAt}`
      });
    } catch {
      // A Central continua funcional mesmo se o snapshot de build não estiver disponível.
    }
  }

  private replaceItem(pageTitle:string,itemTitle:string,patch:Partial<Item>):void {
    const target=this.pages[pageTitle]?.items.find(item=>item.title===itemTitle);
    if (target) Object.assign(target,patch);
  }
}
