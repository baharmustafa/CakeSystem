package uni.fmi.masters.agent;

import java.util.ArrayList;
import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import uni.fmi.masters.Cake;
import uni.fmi.masters.ontology.CakeOntology;

public class BakeryAgent extends Agent {
private CakeOntology cakeOntology;
	
	@Override
	protected void setup() {
		
		//cakeOntology = new CakeOntology();		
		
		DFAgentDescription dfd = new DFAgentDescription();		
		dfd.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("cakeshop");
		sd.setName("cakeshop"); //kupuvachite shte tursqt usluga 
		
		dfd.addServices(sd); //dobavena usluga serves description
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {			
			e.printStackTrace();
		}
		
		addBehaviour(new CakeRequestedBehaviour());
		addBehaviour(new SellCakeBehaviour());
		
	}
	//изчаква съобщение от агента, койти пита за торта
	private class CakeRequestedBehaviour extends CyclicBehaviour{

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			
			ACLMessage msg = myAgent.receive(mt);
			
			if(msg != null) {
				String askedTopping = msg.getContent();
				
				ACLMessage reply = msg.createReply();
				
				ArrayList<Cake> foundCakes = cakeOntology.getCakeByTopping(askedTopping);
				
				double mostToppings = 0;
				
				for(Cake cake : foundCakes) {
					if(cake.getIngridients().size() > mostToppings)
						mostToppings = cake.getIngridients().size();
				}
				
				if (mostToppings != 0) {
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(mostToppings + "");
				} else {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("no cake");
				}
				
				
				//Double price = null;
				
//				for(Cake cake : menu) {
//					if(cake.name.equals(askedTopping)) {
//						price = cake.price;
//						break;
//					}
//				}
				
//				if(price != null) {
//					reply.setPerformative(ACLMessage.PROPOSE);
//					reply.setContent(price + "");
//				}else {
//					reply.setPerformative(ACLMessage.REFUSE);
//					reply.setContent("no cake");
//				}
//				
				myAgent.send(reply);		
				
			}
			
		}
		
		
	}
	
	private class SellCakeBehaviour extends CyclicBehaviour{

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			
			ACLMessage msg = myAgent.receive(mt);
			
			if(msg != null) {
				
				String topping = msg.getContent();
				
				ACLMessage reply = msg.createReply();
				
				Random random = new Random();
				
				if(random.nextBoolean()) {
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println("seller:Just sold cake with " + topping);

				}else {
					
					reply.setPerformative(ACLMessage.FAILURE);
					System.out.println("Sller: not available any longer!");					
				}
				
				myAgent.send(reply);
			}
			
		}	
		
	}
}
