import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ConsoleComponent } from './components/console/console.component';
import { StartAgentComponent } from './components/start-agent/start-agent.component';
import { AgentTypesComponent } from './components/agent-types/agent-types.component';
import { RunningAgentsComponent } from './components/running-agents/running-agents.component';
import { SendMessageComponent } from './components/send-message/send-message.component';

import {MatListModule} from '@angular/material/list'; 
import {MatSelectModule} from '@angular/material/select'; 
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button'
import { FormsModule } from '@angular/forms';
import { HomeComponent } from './components/home/home.component';
import { PredictionComponent } from './components/prediction/prediction.component';

@NgModule({
  declarations: [
    AppComponent,
    ConsoleComponent,
    StartAgentComponent,
    AgentTypesComponent,
    RunningAgentsComponent,
    SendMessageComponent,
    HomeComponent,
    PredictionComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    CommonModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatListModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
