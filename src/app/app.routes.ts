import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard.component';
import { FunctionalitiesComponent } from './pages/functionalities.component';
import { FeatureFormComponent } from './pages/feature-form.component';
import { ModulesComponent } from './pages/modules.component';
import { IdeasComponent } from './pages/ideas.component';
import { DocumentationComponent } from './pages/documentation.component';
import { RoadmapComponent } from './pages/roadmap.component';
import { SimplePageComponent } from './pages/simple-page.component';
import { ReferencesComponent } from './pages/references.component';

export const routes: Routes = [
  {path:'', component:DashboardComponent},
  {path:'dashboard', component:DashboardComponent},
  {path:'funcionalidades', component:FunctionalitiesComponent},
  {path:'funcionalidades/nova', component:FeatureFormComponent},
  {path:'modulos', component:ModulesComponent},
  {path:'ideias', component:IdeasComponent},
  {path:'documentacao', component:DocumentationComponent},
  {path:'roadmap', component:RoadmapComponent},
  {path:'regras', component:SimplePageComponent, data:{title:'Regras de Negócio', icon:'▤'}},
  {path:'referencias', component:ReferencesComponent},
  {path:'dependencias', component:SimplePageComponent, data:{title:'Dependências', icon:'⌘'}},
  {path:'testes', component:SimplePageComponent, data:{title:'Testes', icon:'☑'}},
  {path:'historico', component:SimplePageComponent, data:{title:'Histórico', icon:'↶'}},
  {path:'github', component:SimplePageComponent, data:{title:'GitHub', icon:'↗'}},
  {path:'configuracoes', component:SimplePageComponent, data:{title:'Configurações', icon:'⚙'}}
];
