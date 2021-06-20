import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MatchService {

  baseUrl : string = 'http://localhost:8080/chat-war/rest/match/'

  constructor(private http : HttpClient) { }

  predict(team1 : string, team2 : string) : Observable<any> {
    return this.http.post(this.baseUrl + team1 + '/' + team2, null);
  }
}
