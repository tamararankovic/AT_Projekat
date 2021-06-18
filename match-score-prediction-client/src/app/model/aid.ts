import { AgentType } from "./agent-type";
import { AgentCenter } from "./agent-center";

export class AID {
    constructor(
        public name : string,
        public host : AgentCenter,
        public type : AgentType
    ) {}
}
