import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  showStartAgent = false;
  showSendMessage = false;

  constructor() { }

  ngOnInit(): void {
  }

  toggleStartAgent() {
    this.showStartAgent = !this.showStartAgent
    if(this.showStartAgent)
      this.showSendMessage = false
  }

  toggleSendMessage() {
    this.showSendMessage = !this.showSendMessage
    if(this.showSendMessage)
      this.showStartAgent = false
  }
}
