import { Component, OnInit } from '@angular/core';
import { MatchSocketService } from 'src/app/services/match-socket.service';
import { MatchService } from 'src/app/services/match.service';

@Component({
  selector: 'app-prediction',
  templateUrl: './prediction.component.html',
  styleUrls: ['./prediction.component.css']
})
export class PredictionComponent implements OnInit {

  liveData$ = this.matchSocket.messages$;

  constructor(private matchService : MatchService, private matchSocket : MatchSocketService) { 
    this.liveData$.subscribe({
      next : msg => this.handleMessage(msg as string)
    });
  }

  team1 : string = ''
  team2 : string = ''
  predicted : boolean = false
  prediction : string = ''
  
  ngOnInit(): void {
    this.matchSocket.connect();
  }

  predict() {
    this.matchService.predict(this.team1, this.team2).subscribe();
  }

  reset() {
    this.predicted = false
    this.prediction = ''
    this.team1 = ''
    this.team2 = ''
  }

  handleMessage(msg : string) {
    this.predicted = true
    this.prediction = msg
  }
}
