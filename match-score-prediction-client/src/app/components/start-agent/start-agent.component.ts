import { Component, OnInit } from '@angular/core';
import { AgentType } from 'src/app/model/agent-type';
import { AgentService } from 'src/app/services/agent.service';

@Component({
  selector: 'app-start-agent',
  templateUrl: './start-agent.component.html',
  styleUrls: ['./start-agent.component.css']
})
export class StartAgentComponent implements OnInit {

  constructor(private agentService : AgentService) { }

  types : AgentType[] = []
  type : AgentType
  agentName : string = ''

  ngOnInit(): void {
    this.agentService.getTypes().subscribe(
      data => this.types = data
    )
  }

  public start() {
    if(this.types.includes(this.type) && this.agentName.length > 0)
      this.agentService.startAgent(this.type, this.agentName).subscribe()
  }

}
