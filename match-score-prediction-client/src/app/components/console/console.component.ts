import { Component, OnInit } from '@angular/core';
import { LoggerSocketService } from 'src/app/services/logger-socket.service';

@Component({
  selector: 'app-console',
  templateUrl: './console.component.html',
  styleUrls: ['./console.component.css']
})
export class ConsoleComponent implements OnInit {

  liveData$ = this.loggerSocket.messages$;

  constructor(private loggerSocket : LoggerSocketService) {
    this.liveData$.subscribe({
      next : msg => this.handleMessage(msg as string)
    });
  }

  logs : string[] = []

  ngOnInit(): void {
    this.loggerSocket.connect()
  }

  handleMessage(msg : string) {
    this.logs.push(msg)
  }

  clear() {
    this.logs = []
  }
}
