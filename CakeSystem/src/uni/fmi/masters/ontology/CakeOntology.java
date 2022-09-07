package uni.fmi.masters.ontology;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.HermiT.datatypes.DatatypeRegistry.AnonymousConstantValue;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import uni.fmi.masters.Cake;

public class CakeOntology {

	private OWLOntologyManager ontoManager;
	private OWLOntology cakeOntology;
	private OWLDataFactory dataFactory;
	
	private String ontologyIRIStr; //identifikacia
	private boolean contains = false;
	
	public CakeOntology() {
		ontoManager = OWLManager.createOWLOntologyManager();
		dataFactory = ontoManager.getOWLDataFactory();
		
		loadOntologyFile();
		
		ontologyIRIStr = cakeOntology.getOntologyID().getOntologyIRI().toString() + "#";
	}


	private void loadOntologyFile() {
		File ontoFile = new File("src/ontology_file/cake.owl");
		
		try {
			cakeOntology = ontoManager.loadOntologyFromOntologyDocument(ontoFile);
		
		} catch (OWLOntologyCreationException e) {			
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<Cake> getCakeByTopping(String topping){
		
		ArrayList<Cake> foundCakes = new ArrayList<>();
		
		OWLObjectProperty hasToppingProp = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasTopping"));
		//search topping
		OWLClass toppingClass =  dataFactory.getOWLClass(IRI.create(ontologyIRIStr + topping));
		//а всяка аксиома свързана с топинг ги обхождаме
		for(OWLAxiom axiom : toppingClass.getReferencingAxioms(cakeOntology)) {
			
			//проверка sublcass - ако е от субклас взимаме всички свойства и ги обхождаме
			if(axiom.getAxiomType() == AxiomType.SUBCLASS_OF) {
				
				//Обхожда properties
				for(OWLObjectProperty op : axiom.getObjectPropertiesInSignature()) {
					//проверяваме дали това е от тип торта 
					if(op.getIRI().equals(hasToppingProp.getIRI())) {
						
						for(OWLClass classInAxiom : axiom.getClassesInSignature()) {
							//получаваме отговор
							if(containsSuperClass(classInAxiom.getSuperClasses(cakeOntology),
									dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Cake")))) {
								contains = false;
								
								Cake cake = new Cake();
								
								cake.setName(getClassFriendlyName(classInAxiom));//вземи име
								cake.setId(classInAxiom.getIRI().toString());
								
								cake.setIngridients(getAllCakeTopings(classInAxiom, hasToppingProp));//вземем състваките на пицата
								//добавяме тортата към намерените
								foundCakes.add(cake);								
							}							
						}						
					}					
				}				
			}			
		}	
		
		return foundCakes;
	}


	private ArrayList<String> getAllCakeTopings(OWLClass cakeClass, OWLObjectProperty hasToppingProp) {
		ArrayList<String> result = new ArrayList<>();
		
		for(OWLAxiom axiom : cakeClass.getReferencingAxioms(cakeOntology)) {
			
			if(axiom.getAxiomType() == AxiomType.SUBCLASS_OF) {
				
				for(OWLObjectProperty op: axiom.getObjectPropertiesInSignature()) {
					
					if(op.getIRI().equals(hasToppingProp.getIRI())) {
					
						for(OWLClass classInAxiom : axiom.getClassesInSignature()) {
							
							if(!classInAxiom.getIRI().equals(cakeClass.getIRI())) {
								result.add(getClassFriendlyName(classInAxiom));
							}							
						}						
					}
				}
			}			
		}
		
		return result;
	}


	private String getClassFriendlyName(OWLClass classInAxiom) {
		String label = getEntityLabel(classInAxiom.getAnnotations(cakeOntology), true);
		
		if(label == null || label.isEmpty()) {
			label = getClassName(classInAxiom.getIRI());
		}
		
		return label;		
	}
	
	private String getEntityLabel(Set<OWLAnnotation> annotations, boolean justForLanguage) {
		String label = null;
		String lang = "bg";
		
		for(OWLAnnotation annotation : annotations) {
			if(annotation.getProperty().isLabel()) {
				
				OWLLiteral lit = (OWLLiteral) annotation.getValue();
				
				int index = lit.toString().indexOf('@') + 1;
				
				if(lit.toString().substring(index).equals(lang)) {
					label = lit.getLiteral();
				}
				
				if(label == null && !justForLanguage) {
					label = lit.getLiteral();
				}				
			}
		}
		
		return label;
	}
	

	private String getClassName(IRI iri) {
		
		String IRIStr = iri.toString();
		IRIStr = IRIStr.substring(IRIStr.indexOf('#') + 1);
		
		return IRIStr;
	}

	private boolean containsSuperClass(Set<OWLClassExpression> superClasses, OWLClass owlClass) {
		
		for(OWLClassExpression clasExpr : superClasses) {			
			for(OWLClass cls : clasExpr.getClassesInSignature()) {
				if(cls.getIRI().equals(owlClass.getIRI())) {
					contains = true;
				}else {
					if(cls.getSubClasses(cakeOntology).size() > 0) {
						containsSuperClass(cls.getSuperClasses(cakeOntology), owlClass);
					}
				}
			}						
		}
		
		return contains;
		
	}
	
	
	//Добавяне на класове към онтологията
	public void insert() {
		
		OWLClass myClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Test"));
		
		//roditel
		OWLClass namedCakeClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "NameCake"));
		
		OWLSubClassOfAxiom subClassOf = dataFactory.getOWLSubClassOfAxiom(myClass, namedCakeClass);
		
		AddAxiom axiom = new AddAxiom(cakeOntology, subClassOf);
		
		ontoManager.applyChange(axiom);
		
		try {
			ontoManager.saveOntology(cakeOntology);
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//Премахване на класове от онтологията (за пример)
	public void remove() {
		
		OWLClass myClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Test"));
		
		OWLEntityRemover remover = new OWLEntityRemover(ontoManager, Collections.singleton(cakeOntology));
		
		myClass.accept(remover);
	
		ontoManager.applyChanges(remover.getChanges());
		
		try {
			ontoManager.saveOntology(cakeOntology);
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
}

