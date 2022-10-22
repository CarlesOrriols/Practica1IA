package aima;

import IA.Energia.Centrales;
import IA.Energia.Clientes;
import IA.Energia.VEnergia;

import java.util.Random;

public class EnergiaEstado {
    public static String MOVIMIENTO = "Movimiento";
    public static String INTERCAMBIO = "Intercambio";
    public static String VACIADO = "Vaciado";

    private int[] clientes_asignados;
    private double beneficio;
    private double[] energia_servida;
    private static Centrales centrales;
    private static Clientes clientes;
    private static double[][] distancias; // [central][cliente]

    public EnergiaEstado(EnergiaEstado energiaEstado) {
        this.clientes_asignados = new int[energiaEstado.clientes_asignados.length];
        this.energia_servida = new double[energiaEstado.energia_servida.length];
        this.beneficio = energiaEstado.beneficio;

        for(int i = 0; i < clientes_asignados.length; ++i) {
            this.clientes_asignados[i] = energiaEstado.clientes_asignados[i];
        }
        for(int i = 0; i < energia_servida.length; ++i) {
            this.energia_servida[i] = energiaEstado.energia_servida[i];
        }
    }

    /*
    * Creamos un estado inicial assignando solo los clientes con prioridad garantizada en una central aleatoria,
    * controlando que la central tenga capacidad!
    */
    public EnergiaEstado(Centrales ce, Clientes cl, int semilla) { // Random estado inicial
        // Fijamos los static de centrales y clientes
        centrales = ce;
        clientes = cl;
        distancias = new double[ce.size()][cl.size()];

        // Precalcular distancias entre centrales y clientes
        for (int i_central = 0; i_central < centrales.size(); i_central++) {
            for (int i_cliente = 0; i_cliente < clientes.size(); i_cliente++) {
                int ce_x = centrales.get(i_central).getCoordX();
                int ce_y = centrales.get(i_central).getCoordY();
                int cl_x = clientes.get(i_cliente).getCoordX();
                int cl_y = clientes.get(i_cliente).getCoordY();

                distancias[i_central][i_cliente] = Math.sqrt( Math.pow(ce_x-cl_x, 2) + Math.pow(ce_y-cl_y, 2)); // sqrt( dX^2 + dY^2)
            }
        }

        beneficio = 0.0;     // beneficio si no hay ningun cliente asignado (mas abajo restamos la penalizacion de los clientes no asignados)
        energia_servida = new double[ce.size()];
        for (int i = 0; i < energia_servida.length; i++) { // inicializamos todas las energias ocupadas a 0
            energia_servida[i] = 0.0;
        }
        clientes_asignados = new int[clientes.size()];

        Random random = new Random((long) semilla);

        // Pre-inicializamos todos los clientes sin asignar ninguna central (o sea todos a -1)
        for (int i_cliente = 0; i_cliente < clientes_asignados.length; i_cliente++) {
            clientes_asignados[i_cliente] = -1;
        }

        // Asignamos los clientes a una central random, si no puede ser asignado por una restriccion del problema, se asigna a otra tambien de forma aleatoria
        for (int i_cliente = 0; i_cliente < clientes_asignados.length; i_cliente++) {
            if ( clientes.get(i_cliente).getContrato() == 0 ) { // clientes garantizados se asignan a una central
                boolean cli_asignado = false;
                while ( !cli_asignado ) {
                    int central_random = random.nextInt(centrales.size());
                    if (sePuedeMoverCliente(i_cliente, central_random)) {
                        moverCliente(i_cliente, central_random);
                        cli_asignado = true;
                    }
                }
            } else { // clientes no garantizados se actualiza el beneficio con la penalizacion pertinente
                try {
                    beneficio -= precioPenalizacion(i_cliente);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    /*
     * OPERADORES
     */
    // Pre: Que el cliente tenga lugar en la central destino o lo quiten de la central y sea NO garantizado
    public void moverCliente(int i_cliente, int centralDestino) {
        // Actualizar energia asignada
        int centralAntigua = clientes_asignados[i_cliente];
        if (centralAntigua == -1 && centralDestino != -1) { //Cliente NO asignado -> Asignado
            beneficio += clientes.get(i_cliente).getConsumo() * precioMwCliente(i_cliente) + precioPenalizacion(i_cliente);
            if (energia_servida[centralDestino] == 0.0) { //se tiene que encender
                beneficio -= costeCentralEncendida((centralDestino));
            }
            energia_servida[centralDestino] += consumoMasPerdidas(centralDestino, i_cliente);
        }
        else if (centralAntigua != -1 && centralDestino == -1) { //Cliente Asignado -> NO asignado
            beneficio -= clientes.get(i_cliente).getConsumo() * precioMwCliente(i_cliente) + precioPenalizacion(i_cliente);
            energia_servida[centralAntigua] -= consumoMasPerdidas(centralAntigua, i_cliente);
            if (energia_servida[centralAntigua] == 0.0) {
                beneficio -= costeCentralParada(centralAntigua);
                beneficio += costeCentralEncendida(centralAntigua);
            }
        }
        else if (centralAntigua != -1 && centralDestino != -1) { //cliente cambia de central
            energia_servida[centralAntigua] -= consumoMasPerdidas(centralAntigua, i_cliente);
            if (energia_servida[centralAntigua] == 0.0) {
                beneficio -= costeCentralParada(centralAntigua);
                beneficio += costeCentralEncendida(centralAntigua);
            }
            if (energia_servida[centralDestino] == 0.0) { //se tiene que encender
                beneficio += costeCentralParada(centralDestino);
                beneficio -= costeCentralEncendida(centralDestino);
            }
            energia_servida[centralDestino] += consumoMasPerdidas(centralDestino, i_cliente);
        }
        // Actualizar clientes asignados
        clientes_asignados[i_cliente] = centralDestino;
    }

    // Intercanvia la centrals del client i amb la del client j.
    public void intercambiarClientes(int i, int j) {
        int i_central = clientes_asignados[i];
        int j_central = clientes_asignados[j];
        if (i_central != -1 && j_central != -1) {
            energia_servida[i_central] -= consumoMasPerdidas(i_central, i);
            energia_servida[i_central] += consumoMasPerdidas(i_central, j);
            energia_servida[j_central] -= consumoMasPerdidas(j_central, j);
            energia_servida[j_central] += consumoMasPerdidas(j_central, i);
        }
        if (i_central == -1 && j_central != -1) {
            energia_servida[j_central] -= consumoMasPerdidas(j_central, j);
            energia_servida[j_central] += consumoMasPerdidas(j_central, i);
            beneficio -= clientes.get(j).getConsumo() * precioMwCliente(j) + precioPenalizacion(j);
            beneficio += clientes.get(i).getConsumo() * precioMwCliente(i) + precioPenalizacion(i);
        }
        if (i_central != -1 && j_central == -1) {
            energia_servida[i_central] -= consumoMasPerdidas(i_central, i);
            energia_servida[i_central] += consumoMasPerdidas(i_central, j);
            beneficio -= clientes.get(i).getConsumo() * precioMwCliente(i) + precioPenalizacion(i);
            beneficio += clientes.get(j).getConsumo() * precioMwCliente(j) + precioPenalizacion(j);
        }
        clientes_asignados[i] = j_central;
        clientes_asignados[j] = i_central;
    }

    // Mou tots els clients de la central i a la central j.
    public void vaciarCentral(int cOrigen, int cDestino) {
        if (cOrigen == -1 && cDestino != -1) { // Asignamos todos los clientes de fuera a cDestino
            if (energia_servida[cDestino] == 0.0) {
                beneficio -= costeCentralEncendida(cDestino);
                beneficio += costeCentralParada(cDestino);
            }
            for (int c = 0; c < clientes_asignados.length; c++) {
                if (clientes_asignados[c] == -1) {
                    energia_servida[cDestino] += consumoMasPerdidas(cDestino, c);
                    beneficio += clientes.get(c).getConsumo() * precioMwCliente(c) + precioPenalizacion(c);
                    clientes_asignados[c] = cDestino;
                }
            }
        }
        else if (cOrigen != -1 && cDestino == -1){ // Desasignamos clientes de una cOrigen
            for (int c = 0; c < clientes_asignados.length; c++) {
                if (clientes_asignados[c] == cOrigen) {
                    beneficio -= clientes.get(c).getConsumo() * precioMwCliente(c) + precioPenalizacion(c);
                    clientes_asignados[c] = -1;
                }
            }
            energia_servida[cOrigen] = 0.0;
            beneficio -= costeCentralParada(cOrigen);
            beneficio += costeCentralEncendida(cOrigen);
        }
        else { // Volcamos una central a otra (ninguna esta fuera)
            energia_servida[cOrigen] = 0.0;
            beneficio -= costeCentralParada(cOrigen);
            beneficio += costeCentralEncendida(cOrigen);
            for (int c = 0; c < clientes_asignados.length; c++) {
                if (clientes_asignados[c] == cOrigen) {
                    clientes_asignados[c] = cDestino;
                    energia_servida[cDestino] += consumoMasPerdidas(cDestino,c);
                }
            }
        }
    }



    /*
     * COMPROVADORES
     */
    // Comprovamos que si la central destino es fuera, el cliente sea NO prioritario/garantizado,
    // de lo contrario tenga sitio para este cliente

    public boolean sePuedeMoverCliente(int i_cliente, int i_centralDestino) {
        // (Si lo queremos desasignar y es no garantizado) o cabe en la central destino
        if (clientes_asignados[i_cliente] == i_centralDestino){ // Si la central es la misma que ya esta asignado
            return false;
        }
        if ( i_centralDestino == -1 && clientes.get(i_cliente).getContrato() == 0) { // Vas a no subministrar el cliente y es garantizado
            return false;
        }
        if (i_centralDestino != -1 && consumoMasPerdidas(i_centralDestino, i_cliente) > energiaSobranteCentral(i_centralDestino) ) { // Si no estamos desassignando, la central debe tener mas espacio que el consumo necesario
            return false;
        }

        return true;
    }

    // Cert si el client i es pot intercanviar amb el client j
    public boolean sePuedenIntercambiarClientes(int i, int j) {
        int i_central = clientes_asignados[i];
        int j_central = clientes_asignados[j];
        if (i == j) {
            return false;       // no son el mismo client
        }
        else if (i_central == j_central) {
            return false;  //no son de la misma central
        }
        if ( (i_central == -1 && clientes.get(j).getContrato() == 0) || (j_central == -1 && clientes.get(i).getContrato() == 0) ) {//si son garantizado no se pueden desasignar
            return false;
        } else if (i_central != -1 && consumoMasPerdidas(i_central, j) > energiaSobranteCentral(i_central)+consumoMasPerdidas(i_central, i)) { //hay energia suficiente en la i_central para j
            return false;
        } else if (j_central != -1 && consumoMasPerdidas(j_central, i) > energiaSobranteCentral(j_central)+consumoMasPerdidas(j_central, j)) { //hay energia suficiente en la j_central para i
            return false;
        }
        return true;
    }

    // Cert si la central i pot bolcar tots els seus clients a la central j
    public boolean sePuedeVaciarCentral(int cOrigen, int cDestino) {
        if (cOrigen == cDestino) {
            return false;
        }
        if (cOrigen != -1 && cDestino == -1) {
            for (int c = 0; c < clientes_asignados.length; c++) {
                if (clientes_asignados[c] == cOrigen && clientes.get(c).getContrato() == 0) {
                    return false;
                }
            }
        }
        if (cDestino != -1) {
            double sum = 0;
            for (int c = 0; c < clientes_asignados.length; c++) {
                if (clientes_asignados[c] == cOrigen) {
                    sum += consumoMasPerdidas(c, cDestino);
                }
                if (sum > energiaSobranteCentral(cDestino)) {
                    return false;
                }
            }
            if (sum > 0) {
                return true;
            }
        }
        return false;
    }


    // Beneficio obtenido del estado
    public double beneficioTotal() {
        return beneficio;
    }

    // Numero de clientes
    public int getNClientes(){
        return clientes.size();
    }

    // Numero de centrals
    public int getNCentrales(){
        return centrales.size();
    }

    // Imprimir el estado
    public void print() {
        System.out.println("------ ESTADO -------");
        for (int i_cliente = 0; i_cliente < clientes_asignados.length; i_cliente++) { // Centrales
            System.out.println("Cliente " + i_cliente + " -> " + clientes_asignados[i_cliente]);
        }
        // print que li he posat per veure la FH
        System.out.println("Heuristica: " + heuristicFunction());
        System.out.println("---------------------");
    }

    /*
     * FUNCIONES AUXILIARES
     */
    // Consumo de un cliente contando perdidas por la distancia a la central
    private double consumoMasPerdidas (int i_central, int i_cliente) {
        double dist = distancias[i_central][i_cliente];
        return clientes.get(i_cliente).getConsumo() * (1+VEnergia.getPerdida(dist));
    }

    // Cantidad de energia que puede ser asignada aun
    private double energiaSobranteCentral (int i_central) {
        return centrales.get(i_central).getProduccion() - energia_servida[i_central];
    }

    // Valoramos que el precio de Mw pagado por el cliente es el de ser servido por
    private double precioMwCliente(int i_cliente) {
        try {
            if ( clientes.get(i_cliente).getContrato() == 0 ) { // Clientes garantizados
                return VEnergia.getTarifaClienteGarantizada(clientes.get(i_cliente).getTipo());
            } else { // Clientes NO garantizados
                return VEnergia.getTarifaClienteNoGarantizada(clientes.get(i_cliente).getTipo());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Pre: el cliente es no garantizado
    private double precioPenalizacion(int i_cliente) {
        try {
            //Preu per MW no servit!!
            return (clientes.get(i_cliente).getConsumo() * VEnergia.getTarifaClientePenalizacion(clientes.get(i_cliente).getTipo()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Coste encender central
    private double costeCentralEncendida(int i_central) {
        try {
            int tipo_central = centrales.get(i_central).getTipo();
            return VEnergia.getCosteProduccionMW(tipo_central) * centrales.get(i_central).getProduccion() + VEnergia.getCosteMarcha(tipo_central);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Coste de tener la central parada
    private double costeCentralParada(int i_central) {
        try {
            return VEnergia.getCosteParada(centrales.get(i_central).getTipo());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Funcio heurística de prova (he comentat moltes coses pq nose si al final les necessitarem)
    // TODO: Acabar de mirar la heuristica pq em temo que no acaba de funcionar hehe

    public double heuristicFunction() {
        double suma_ocupacio = 0;
        // double benefici_ideal = 0;
        for (int i = 0; i < getNCentrales(); ++i) {
            suma_ocupacio += (energia_servida[i] / centrales.get(i).getProduccion());
            /*
            if (energia_servida[i] != 0)
                benefici_ideal -= costeCentralEncendida(i);
            */
        }
        suma_ocupacio /= getNCentrales();

        double  suma_distancia = 0;
        for (int i = 0; i < clientes_asignados.length; ++i) {
            if (clientes_asignados[i] != -1) {
                // restant 10000 a la distancia aconseguirem que la distancia més llarga ens doni el resultat més petit
                suma_distancia += (10000 -  distancias[clientes_asignados[i]][i]) / 10000;
            }
            /* Coses que he comentat perquè de moment veig innecessaries
            double consum = clientes.get(i).getConsumo();
            if (clientes.get(i).getTipo() == 0) {
                if (consum > 5) benefici_ideal += 400 * consum;
                else if (consum <= 2) benefici_ideal += 600 * consum;
                else benefici_ideal += 500 * consum;
            }
            else {
                if (consum > 5) benefici_ideal += 300 * consum;
                else if (consum <= 2) benefici_ideal += 500 * consum;
                else benefici_ideal += 400 * consum;
            }
            */
        }
        suma_distancia /= getNClientes();
        // double benefici = (beneficioTotal() / benefici_ideal) * 100;


        return beneficioTotal() * (suma_ocupacio * 0.7 + suma_distancia * 0.3);
    }

}
