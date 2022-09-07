package uni.fmi.masters.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import uni.fmi.masters.gui.BuyerAgentGUI;

public class BuyerAgent extends Agent {

	public String searchedTopping = null;
	private String name;
	AID[] cakeshops;
	
	int messageCounter = 0;	
	double lowestPrice = 0;
	AID cheapestCake = null;
	
	BuyerAgentGUI gui;
	
	@Override
	protected void takeDown() {
		gui.hide();
	}

	@Override
	protected void setup() {
		gui = new BuyerAgentGUI(this);
		name = getAID().getName();
	
		System.out.println(name + ":Hello i`m hungry...");
		
		addBehaviour(new TickerBehaviour(this, 15000) {
			
			@Override
			protected void onTick() {
				if(searchedTopping != null) {
					System.out.println(name + ":Hungry for something with " + searchedTopping);
					
					DFAgentDescription searchDescription = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("cakeshop");
					sd.setName("cakeshop");
					
					searchDescription.addServices(sd);
					
					try {
						DFAgentDescription[] listOfDescriptions = DFService.search(myAgent, searchDescription);
						cakeshops = new AID[listOfDescriptions.length];
						
						for(int i = 0; i < listOfDescriptions.length; i++) {
							cakeshops[i] = listOfDescriptions[i].getName();
						}
						
						if(cakeshops.length > 0) {
							myAgent.addBehaviour(new SearchForCakeBehaviour());
						}
						
					} catch (FIPAException e) {
						e.printStackTrace();
					}
					
				}else {
					System.out.println(name + ":Not hungry yet");
				}
			}
		});
		
		
	}
	
	private class SearchForCakeBehaviour extends Behaviour{

		int step = 0;
		MessageTemplate mt;
		
		@Override
		public void action() {
			switch(step) {
			
			case 0:
				//изпращане на съобщение към всички сладкарници
				//cfp - за първоначални запитвания
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				
				for(int i = 0; i < cakeshops.length; i++) {
					cfp.addReceiver(cakeshops[i]);
				}
				
				cfp.setContent(searchedTopping);
				cfp.setConversationId("cake stuff");
				cfp.setReplyWith("cfp" + System.currentTimeMillis());
				
				myAgent.send(cfp);
				
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("cake stuff"), 
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				
				step = 1;
				
				break;
			case 1:
				
				ACLMessage msg = myAgent.receive(mt);
				
				if(msg != null) {
					
					if(msg.getPerformative() == ACLMessage.PROPOSE) {
						
						double price = Double.parseDouble(msg.getContent());
						
						if(cheapestCake == null || price < lowestPrice) {
							cheapestCake = msg.getSender();
							lowestPrice = price;							
						}
						
					}
					
					messageCounter++;
					
					if(messageCounter >= cakeshops.length) {
						step++;
					}
				}
								
				break;
				
			case 2:
				if(cheapestCake != null) {
					ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					order.addReceiver(cheapestCake);
					order.setContent(searchedTopping);
					
					order.setConversationId("cake stuff");
					order.setReplyWith("order" + System.currentTimeMillis());
					
					myAgent.send(order);
					
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("pizza stuff"),
							MessageTemplate.MatchInReplyTo(order.getReplyWith()));
							
					step++;
					
				}		
				
				break;
			
			case 3:
				
				ACLMessage finalMsg = myAgent.receive(mt);
				
				if(finalMsg != null) {
					
					if(finalMsg.getPerformative() == ACLMessage.INFORM) {
						System.out.println(name + ": Got his cake and is no longer hungry!");
						myAgent.doDelete();
					}else {
						System.out.println(name + "No c :((((((");
					}
					
					step++;
					
				}
				
				break;
			}
			
		}

		@Override
		public boolean done() {
			if(step == 2 && cheapestCake == null)
			{
				System.out.println("No such cake found for sell. Will wait and try again later!");
				return true;
			}else if(step == 4) {
				System.out.println("Waiting for another try.");
				return true;
			}
			
			return false;
		}
		
		
	}
	
}
				
		
	