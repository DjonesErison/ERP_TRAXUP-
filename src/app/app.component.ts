import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector:'app-root',
  standalone:true,
  imports:[RouterOutlet, RouterLink, RouterLinkActive],
  template: `
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand"><img src="/assets/traxup-logo.webp" alt="TRAXUP"><div><b>Central</b><small>Gerenciador do Produto</small></div></div>
      <nav>
        <a routerLink="/dashboard" routerLinkActive="active">🏠 <span>Dashboard</span></a>
        <a routerLink="/modulos" routerLinkActive="active">▦ <span>Módulos</span></a>
        <a routerLink="/funcionalidades" routerLinkActive="active">⚙ <span>Funcionalidades</span></a>
        <a routerLink="/ideias" routerLinkActive="active">💡 <span>Ideias</span></a>
        <a routerLink="/regras" routerLinkActive="active">▤ <span>Regras de Negócio</span></a>
        <a routerLink="/referencias" routerLinkActive="active">▧ <span>Referências / Imagens</span></a>
        <a routerLink="/dependencias" routerLinkActive="active">⌘ <span>Dependências</span></a>
        <a routerLink="/testes" routerLinkActive="active">☑ <span>Testes</span></a>
        <a routerLink="/documentacao" routerLinkActive="active">□ <span>Documentação</span></a>
        <a routerLink="/roadmap" routerLinkActive="active">⚑ <span>Roadmap</span></a>
        <a routerLink="/historico" routerLinkActive="active">↶ <span>Histórico</span></a>
        <a routerLink="/github" routerLinkActive="active">↗ <span>GitHub</span></a>
        <a routerLink="/configuracoes" routerLinkActive="active">⚙ <span>Configurações</span></a>
      </nav>
      <small class="version">v1.0.0 • Angular 19</small>
    </aside>
    <main class="main">
      <header class="topbar">
        <div class="mobile-title">☰ <strong>TRAXUP Central</strong></div>
        <input class="global-search" placeholder="⌕  Pesquisar módulos, funcionalidades e documentos...">
        <div class="user"><span class="notification">♧<i></i></span><div class="avatar">D</div><div><b>Djones</b><small>Administrador</small></div></div>
      </header>
      <router-outlet />
    </main>
  </div>`
})
export class AppComponent {}
