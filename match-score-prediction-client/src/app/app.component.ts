import { Component } from '@angular/core';
import { AgentType } from './model/agent-type';
import { AID } from './model/aid';
import { AgentService } from './services/agent.service';
import { MessageService } from './services/message.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'match-score-prediction-client';

  showStartAgent = false;
  showSendMessage = false;

  constructor() {
  }
}
