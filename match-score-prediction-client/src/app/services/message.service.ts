import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ACLMessage } from '../model/acl-message';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  baseUrl : string = 'http://localhost:8080/chat-war/rest/messages/'

  constructor(private http: HttpClient) { }

  sendMessage(message: ACLMessage) : Observable<any> {
    return this.http.post(this.baseUrl, message);
  }

  getPerformatives() : Observable<string[]> {
    return this.http.get<string[]>(this.baseUrl);
  }
}
