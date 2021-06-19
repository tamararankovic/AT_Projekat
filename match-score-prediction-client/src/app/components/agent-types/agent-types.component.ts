import { Component, OnInit } from '@angular/core';
import { AgentType } from 'src/app/model/agent-type';
import { AgentService } from 'src/app/services/agent.service';
import { TypeSocketService } from 'src/app/services/type-socket.service';

@Component({
  selector: 'app-agent-types',
  templateUrl: './agent-types.component.html',
  styleUrls: ['./agent-types.component.css']
})
export class AgentTypesComponent implements OnInit {

  liveData$ = this.typeSocket.messages$;

  constructor(private agentService : AgentService, private typeSocket : TypeSocketService) {
    this.liveData$.subscribe({
      next : msg => this.handleMessage(msg as string)
    });
  }

  types : AgentType[] = []

  ngOnInit(): void {
    this.typeSocket.connect();
    this.agentService.getTypes().subscribe(
      data => this.types = data
    )
  }

  handleMessage(msg : string) {
    this.types = JSON.parse(msg)
  }
}
