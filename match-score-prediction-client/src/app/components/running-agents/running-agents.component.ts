import { Component, OnInit } from '@angular/core';
import { AID } from 'src/app/model/aid';
import { AgentSocketService } from 'src/app/services/agent-socket.service';
import { AgentService } from 'src/app/services/agent.service';

@Component({
  selector: 'app-running-agents',
  templateUrl: './running-agents.component.html',
  styleUrls: ['./running-agents.component.css']
})
export class RunningAgentsComponent implements OnInit {

  liveData$ = this.agentSocket.messages$;

  constructor(private agentService : AgentService, private agentSocket : AgentSocketService) {
    this.liveData$.subscribe({
      next : msg => this.handleMessage(msg as string)
    });
  }

  running : AID[] = []
  
  ngOnInit(): void {
    this.agentSocket.connect();
    this.agentService.getRunningAgents().subscribe(
      data => this.running = data
    )
  }

  public stop(agent : AID) {
    this.agentService.stopAgent(agent).subscribe()
  }

  handleMessage(msg : string) {
    this.running = JSON.parse(msg)
  }
}
