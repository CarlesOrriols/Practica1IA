import IA.Energia.Centrales;
import IA.Energia.Clientes;
import IA.Energia.VEnergia;
import IA.probTSP.ProbTSPGoalTest;
import IA.probTSP.ProbTSPHeuristicFunction;
import IA.probTSP.ProbTSPSuccessorFunctionSA;
import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

public class Main {

    public static void main(String[] args) throws Exception {
//        System.out.println("Hello world!");
        int semilla_random_centrales = 100; // -sce

        int semilla_random_clientes = 7; // -scl

        int num_clientes = 1000; // -ncl

        int[] num_tipos_centrales = new int[] {5,10,25}; // -ntce
        double[] proporciones_cli = new double[] {0.25, 0.3, 0.45}; // -pcli
        double proporcion_garantizado = 0.75; // -pg

        int semilla_random_estado_inicial = 8; // -sei

        String hillClimbing_or_simulatedAnnealing = "hc"; // -hcorsa (hc / sa)
        String tipo_estado_inicial = "greedy"; // -ei (random / greedy)

        for (int i=0; i < args.length; i+=2) {
            switch(args[i]) {
                case "-sce":
                    semilla_random_centrales = Integer.parseInt(args[i+1]);
                    break;
                case "-scl":
                    semilla_random_clientes = Integer.parseInt(args[i+1]);
                    break;
                case "-ncl":
                    num_clientes = Integer.parseInt(args[i+1]);
                    break;
                case "-sei":
                    semilla_random_estado_inicial = Integer.parseInt(args[i+1]);
                    break;
                case "-pg":
                    proporcion_garantizado = Double.parseDouble(args[i+1]);
                    break;
                case "-ntce":
                    num_tipos_centrales = new int[] {Integer.parseInt(args[i+1]),Integer.parseInt(args[i+2]),Integer.parseInt(args[i+3])};
                    i+=2;
                    break;
                case "-pcli":
                    proporciones_cli = new double[] {Double.parseDouble(args[i+1]),Double.parseDouble(args[i+2]),Double.parseDouble(args[i+3])};
                    i+=2;
                    break;
                case "-hcorsa":
                    hillClimbing_or_simulatedAnnealing = args[i+1]; // per defecte esta hillclimbing
                    break;
                case "-ei":
                    tipo_estado_inicial = args[i+1]; // defecte random
                    break;
            }
        }


        Centrales centrales = new Centrales(num_tipos_centrales, semilla_random_centrales);


        Clientes clientes = new Clientes(num_clientes, proporciones_cli, proporcion_garantizado, semilla_random_clientes);

//        print(centrales);
//        print(clientes);
//        print_energia();
        long startTime = System.nanoTime();

        EnergiaEstado estado_inicial;
        if ( tipo_estado_inicial == "random" ) {
            estado_inicial = new EnergiaEstado(centrales, clientes, semilla_random_estado_inicial);
        } else { // greedy
            estado_inicial = new EnergiaEstado(centrales, clientes);
        }
        estado_inicial.print();

        if ( hillClimbing_or_simulatedAnnealing == "hc" ){
            Search typeOfSearch = new HillClimbingSearch();
            SuccessorFunction successorFunction =  new EnergiaSuccessorFunctionHillClimbing();
            HeuristicFunction heuristicFunction = new EnergiaHeuristicFunction();

            Problem problem = new Problem(estado_inicial, successorFunction, new EnergiaGoalTest(), heuristicFunction);
            SearchAgent agent = new SearchAgent(problem, typeOfSearch);
            EnergiaEstado goalState = (EnergiaEstado) typeOfSearch.getGoalState();
            goalState.print();

        } else if( hillClimbing_or_simulatedAnnealing == "sa" ) {
            SuccessorFunction successorFunction = new EnergiaSuccessorFunctionSimmulatedAnnealing();
            HeuristicFunction heuristicFunction = new EnergiaHeuristicFunction();
            SimulatedAnnealingSearch search = new SimulatedAnnealingSearch(2000, 100, 5, 0.001);

            Problem problem = new Problem(estado_inicial, successorFunction, new EnergiaGoalTest(), heuristicFunction);
            SearchAgent agent = new SearchAgent(problem, search);
            EnergiaEstado goalState = (EnergiaEstado) search.getGoalState();
            goalState.print();

        }



        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1000000000.0 + " seconds");
    }

    private static void print(Centrales centrales) {
        System.out.println("------ CENTRALES ------");
        for (int i = 0; i < centrales.size(); i++) {
            System.out.println("Central -> " + i);
            System.out.println("\tX -> " + centrales.get(i).getCoordX());
            System.out.println("\tY -> " + centrales.get(i).getCoordY());
            System.out.println("\tProd -> " + centrales.get(i).getProduccion());
            System.out.println("\tTipo -> " + centrales.get(i).getTipo());
        }
        System.out.println("-----------------------");
    }

    private static void print(Clientes clientes) {
        System.out.println("------ CLIENTES ------");
        for (int i = 0; i < clientes.size(); i++) {
            System.out.println("Cliente -> " + i);
            System.out.println("\tX -> " + clientes.get(i).getCoordX());
            System.out.println("\tY -> " + clientes.get(i).getCoordY());
            System.out.println("\tConsumo -> " + clientes.get(i).getConsumo());
            System.out.println("\tContrato -> " + clientes.get(i).getContrato());
            System.out.println("\tTipo -> " + clientes.get(i).getTipo());
        }
        System.out.println("-----------------------");
    }

    private static void print_energia() {
        try {
            System.out.println("------ DATOS ENERGIA ------");
            System.out.println("XG tarifa garant -> " + VEnergia.getTarifaClienteGarantizada(0));
            System.out.println("MG tarifa garant -> " + VEnergia.getTarifaClienteGarantizada(1));
            System.out.println("G tarifa garant -> " + VEnergia.getTarifaClienteGarantizada(2));
            System.out.println("XG tarifa NO garant -> " + VEnergia.getTarifaClienteNoGarantizada(0));
            System.out.println("MG tarifa NO garant -> " + VEnergia.getTarifaClienteNoGarantizada(1));
            System.out.println("G tarifa NO garant -> " + VEnergia.getTarifaClienteNoGarantizada(2));
            System.out.println("XG penalizacion -> " + VEnergia.getTarifaClientePenalizacion(0));
            System.out.println("MG penalizacion -> " + VEnergia.getTarifaClientePenalizacion(1));
            System.out.println("G penalizacion -> " + VEnergia.getTarifaClientePenalizacion(2));
            System.out.println("A MW -> " + VEnergia.getCosteProduccionMW(0));
            System.out.println("B MW -> " + VEnergia.getCosteProduccionMW(1));
            System.out.println("C MW -> " + VEnergia.getCosteProduccionMW(2));
            System.out.println("A encender -> " + VEnergia.getCosteMarcha(0));
            System.out.println("B encender -> " + VEnergia.getCosteMarcha(1));
            System.out.println("C encender -> " + VEnergia.getCosteMarcha(2));
            System.out.println("A parada -> " + VEnergia.getCosteParada(0));
            System.out.println("B parada -> " + VEnergia.getCosteParada(1));
            System.out.println("C parada -> " + VEnergia.getCosteParada(2));
            System.out.println("Perdida hasta 10km -> " + VEnergia.getPerdida(10));
            System.out.println("Perdida hasta 25km -> " + VEnergia.getPerdida(25));
            System.out.println("Perdida hasta 50km -> " + VEnergia.getPerdida(50));
            System.out.println("Perdida hasta 75km -> " + VEnergia.getPerdida(75));
            System.out.println("Perdida más de 75km + -> " + VEnergia.getPerdida(76));
            System.out.println("-----------------------");
        } catch(Exception e) {
            System.out.println("Problemes amb l'estat de les cuotes d'energia");
        }
    }
}