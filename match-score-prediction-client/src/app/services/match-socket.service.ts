import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { catchError, tap, switchAll, share } from 'rxjs/operators';
import { EMPTY, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MatchSocketService {

  constructor() { }

  private WS_ENDPOINT : string = 'ws://localhost:8080/chat-war/ws/match';

  private socket$: WebSocketSubject<any>;
  private messagesSubject$ = new Subject();
  public messages$ = this.messagesSubject$.pipe(switchAll(), share(), catchError(e => { throw e }));
  
  public connect(): void {
    if (!this.socket$ || this.socket$.closed) {
      this.socket$ = this.getNewWebSocket();
      const messages = this.socket$.pipe(
        tap({
          error: error => console.log(error),
        }), catchError(_ => EMPTY));
      this.messagesSubject$.next(messages);
      this.socket$.subscribe({
        complete: () => { console.log('connection with logger socket closed') }
      })
    }
  }
  
  private getNewWebSocket() {
    return webSocket({url: this.WS_ENDPOINT, deserializer: msg => msg.data});
  }

  sendMessage(msg: any) {
    this.socket$.next(msg);
  }

  close() {
    this.socket$.complete();
  }
}
