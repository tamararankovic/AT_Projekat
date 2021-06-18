import { Component, OnInit } from '@angular/core';
import { AgentType } from 'src/app/model/agent-type';
import { AgentService } from 'src/app/services/agent.service';

@Component({
  selector: 'app-agent-types',
  templateUrl: './agent-types.component.html',
  styleUrls: ['./agent-types.component.css']
})
export class AgentTypesComponent implements OnInit {

  constructor(private agentService : AgentService) { }

  types : AgentType[] = []

  ngOnInit(): void {
    this.agentService.getTypes().subscribe(
      data => this.types = data
    )
  }

}
