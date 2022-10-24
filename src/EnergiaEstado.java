import IA.Energia.Centrales;
import IA.Energia.Clientes;
import IA.Energia.VEnergia;

import java.util.Random;//hola

import static java.lang.Math.sqrt;

public class EnergiaEstado {
    private int[] clientes_asignados;
    private double beneficio;
    private double[] energia_servida;
    private double proporcion_distancias_asignados;
    private double proporcion_ocupacion_encendidas;
    private int n_clientes_asignados;
    private int n_centrales_encendidas;
    private int profundidad_arbol;
    private static Centrales centrales;
    private static Clientes clientes;
    private static double[][] distancias; // [central][cliente]

    public EnergiaEstado(EnergiaEstado energiaEstado) {
        this.clientes_asignados = new int[energiaEstado.clientes_asignados.length];
        this.energia_servida = new double[energiaEstado.energia_servida.length];
        this.beneficio = energiaEstado.beneficio;
        this.n_clientes_asignados = energiaEstado.n_clientes_asignados;
        this.proporcion_distancias_asignados = energiaEstado.proporcion_distancias_asignados;
        this.proporcion_ocupacion_encendidas = energiaEstado.proporcion_ocupacion_encendidas;
        this.n_centrales_encendidas = energiaEstado.n_centrales_encendidas;
        this.profundidad_arbol = energiaEstado.profundidad_arbol + 1;

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
        profundidad_arbol = 0;

        // Precalcular distancias entre centrales y clientes
        for (int i_central = 0; i_central < centrales.size(); i_central++) {
            for (int i_cliente = 0; i_cliente < clientes.size(); i_cliente++) {
                int ce_x = centrales.get(i_central).getCoordX();
                int ce_y = centrales.get(i_central).getCoordY();
                int cl_x = clientes.get(i_cliente).getCoordX();
                int cl_y = clientes.get(i_cliente).getCoordY();

                distancias[i_central][i_cliente] = sqrt( Math.pow(ce_x-cl_x, 2) + Math.pow(ce_y-cl_y, 2)); // sqrt( dX^2 + dY^2)
            }
        }

        n_clientes_asignados = 0;
        proporcion_distancias_asignados = 0.0;
        n_centrales_encendidas = 0;
        proporcion_ocupacion_encendidas = 0.0;
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

        for (int i_central = 0; i_central < centrales.size(); i_central++) {
            beneficio -= costeCentralParada(i_central);
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

    public EnergiaEstado(Centrales ce, Clientes cl) { // Estado inicial asignando el maximo de clientes a las centrales de indice menor
        profundidad_arbol = 0;
        centrales = ce;
        clientes = cl;
        distancias = new double[ce.size()][cl.size()];
        // Fijamos los static de centrales y clientes

        // Precalcular distancias entre centrales y clientes
        for (int i_central = 0; i_central < centrales.size(); i_central++) {
            for (int i_cliente = 0; i_cliente < clientes.size(); i_cliente++) {
                int ce_x = centrales.get(i_central).getCoordX();
                int ce_y = centrales.get(i_central).getCoordY();
                int cl_x = clientes.get(i_cliente).getCoordX();
                int cl_y = clientes.get(i_cliente).getCoordY();

                distancias[i_central][i_cliente] = sqrt(Math.pow(ce_x - cl_x, 2) + Math.pow(ce_y - cl_y, 2)); // sqrt( dX^2 + dY^2)
            }
        }

        n_clientes_asignados = 0;
        proporcion_distancias_asignados = 0.0;
        n_centrales_encendidas = 0;
        proporcion_ocupacion_encendidas = 0.0;

        beneficio = 0.0;     // beneficio si no hay ningun cliente asignado (mas abajo restamos la penalizacion de los clientes no asignados)
        energia_servida = new double[ce.size()];
        for (int i = 0; i < energia_servida.length; i++) { // inicializamos todas las energias ocupadas a 0
            energia_servida[i] = 0.0;
        }
        clientes_asignados = new int[clientes.size()];

        // Pre-inicializamos todos los clientes sin asignar ninguna central (o sea todos a -1)
        for (int i_cliente = 0; i_cliente < clientes_asignados.length; i_cliente++) {
            clientes_asignados[i_cliente] = -1;
        }

        int i_central = 0;
        for (int i_cliente = 0; i_cliente < clientes_asignados.length; i_cliente++) {
            if (clientes.get(i_cliente).getContrato() == 0) { // clientes garantizados se asignan a una central
                boolean cli_asignado = false;
                while ( !cli_asignado ) {
                    if (sePuedeMoverCliente(i_cliente, i_central)) {
                        moverCliente(i_cliente, i_central);
                        cli_asignado = true;
                    } else {
                        i_central++;
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
            n_clientes_asignados++;
            proporcion_distancias_asignados += normalizaDistancia(centralDestino, i_cliente);

            beneficio += clientes.get(i_cliente).getConsumo() * precioMwCliente(i_cliente);
            if (clientes.get(i_cliente).getContrato() == 1) {
                beneficio += precioPenalizacion(i_cliente);
            }
            if (energia_servida[centralDestino] == 0.0) { //se tiene que encender
                beneficio -= costeCentralEncendida(centralDestino);
                beneficio += costeCentralParada(centralDestino);
                n_centrales_encendidas++;
            }
            proporcion_ocupacion_encendidas -= normalizaOcupacion(centralDestino);
            energia_servida[centralDestino] += consumoMasPerdidas(centralDestino, i_cliente);
            proporcion_ocupacion_encendidas += normalizaOcupacion(centralDestino);
        }
        else if (centralAntigua != -1 && centralDestino == -1) { //Cliente Asignado -> NO asignado
            n_clientes_asignados--;
            proporcion_distancias_asignados -= normalizaDistancia(centralAntigua, i_cliente);
            beneficio -= clientes.get(i_cliente).getConsumo() * precioMwCliente(i_cliente) + precioPenalizacion(i_cliente);

            proporcion_ocupacion_encendidas -= normalizaOcupacion(centralAntigua);
            energia_servida[centralAntigua] -= consumoMasPerdidas(centralAntigua, i_cliente);
            proporcion_ocupacion_encendidas += normalizaOcupacion(centralAntigua);
            if (energia_servida[centralAntigua] == 0.0) {
                n_centrales_encendidas--;
                beneficio -= costeCentralParada(centralAntigua);
                beneficio += costeCentralEncendida(centralAntigua);
            }
        }
        else if (centralAntigua != -1 && centralDestino != -1) { //cliente cambia de central
            proporcion_distancias_asignados += normalizaDistancia(centralDestino, i_cliente);
            proporcion_distancias_asignados -= normalizaDistancia(centralAntigua, i_cliente);

            proporcion_ocupacion_encendidas -= normalizaOcupacion(centralAntigua);
            energia_servida[centralAntigua] -= consumoMasPerdidas(centralAntigua, i_cliente);
            proporcion_ocupacion_encendidas += normalizaOcupacion(centralAntigua);
            if (energia_servida[centralAntigua] == 0.0) {
                n_centrales_encendidas--;
                beneficio -= costeCentralParada(centralAntigua);
                beneficio += costeCentralEncendida(centralAntigua);
            }
            if (energia_servida[centralDestino] == 0.0) { //se tiene que encender
                beneficio += costeCentralParada(centralDestino);
                beneficio -= costeCentralEncendida(centralDestino);
                n_centrales_encendidas++;
            }
            proporcion_ocupacion_encendidas -= normalizaOcupacion(centralDestino);
            energia_servida[centralDestino] += consumoMasPerdidas(centralDestino, i_cliente);
            proporcion_ocupacion_encendidas += normalizaOcupacion(centralDestino);
        }
        // Actualizar clientes asignados
        clientes_asignados[i_cliente] = centralDestino;
    }

    // Intercanvia la centrals del client i amb la del client j.
    public void intercambiarClientes(int i, int j) {
        int i_central = clientes_asignados[i];
        int j_central = clientes_asignados[j];
        if (i_central != -1 && j_central != -1) {
            proporcion_distancias_asignados -= normalizaDistancia(i_central, i);
            proporcion_distancias_asignados -= normalizaDistancia(j_central, j);

            proporcion_distancias_asignados += normalizaDistancia(i_central, j);
            proporcion_distancias_asignados += normalizaDistancia(j_central, i);

            proporcion_ocupacion_encendidas -= normalizaOcupacion(i_central);
            energia_servida[i_central] -= consumoMasPerdidas(i_central, i);
            energia_servida[i_central] += consumoMasPerdidas(i_central, j);
            proporcion_ocupacion_encendidas += normalizaOcupacion(i_central);

            proporcion_ocupacion_encendidas -= normalizaOcupacion(j_central);
            energia_servida[j_central] -= consumoMasPerdidas(j_central, j);
            energia_servida[j_central] += consumoMasPerdidas(j_central, i);
            proporcion_ocupacion_encendidas += normalizaOcupacion(j_central);
        }
        if (i_central == -1 && j_central != -1) {
            proporcion_distancias_asignados -= normalizaDistancia(j_central, j);
            proporcion_distancias_asignados += normalizaDistancia(j_central, i);

            proporcion_ocupacion_encendidas -= normalizaOcupacion(j_central);
            energia_servida[j_central] -= consumoMasPerdidas(j_central, j);
            energia_servida[j_central] += consumoMasPerdidas(j_central, i);
            proporcion_ocupacion_encendidas += normalizaOcupacion(j_central);


            beneficio -= clientes.get(j).getConsumo() * precioMwCliente(j) + precioPenalizacion(j);
            beneficio += clientes.get(i).getConsumo() * precioMwCliente(i) + precioPenalizacion(i);
        }
        if (i_central != -1 && j_central == -1) {
            proporcion_distancias_asignados -= normalizaDistancia(i_central, i);
            proporcion_distancias_asignados += normalizaDistancia(i_central, j);

            proporcion_ocupacion_encendidas -= normalizaOcupacion(i_central);
            energia_servida[i_central] -= consumoMasPerdidas(i_central, i);
            energia_servida[i_central] += consumoMasPerdidas(i_central, j);
            proporcion_ocupacion_encendidas += normalizaOcupacion(i_central);

            beneficio -= clientes.get(i).getConsumo() * precioMwCliente(i) + precioPenalizacion(i);
            beneficio += clientes.get(j).getConsumo() * precioMwCliente(j) + precioPenalizacion(j);
        }
        clientes_asignados[i] = j_central;
        clientes_asignados[j] = i_central;
    }

    // Mou tots els clients de la central i a la central j.
    public void vaciarCentral(int cOrigen, int cDestino) {
        if (cOrigen == -1 && cDestino != -1) { // Asignamos todos los clientes posibles de fuera a cDestino
            if (energia_servida[cDestino] == 0.0) { // Si la central destino esta vacia, la encendemos
                beneficio -= costeCentralEncendida(cDestino);
                beneficio += costeCentralParada(cDestino);
                n_centrales_encendidas++;
            }
            int cli = 0;
            boolean ultimo_cliente_assig = true;
            while ( cli < clientes_asignados.length && ultimo_cliente_assig ) {
                if (clientes_asignados[cli] == -1) { // clientes no assignados
                    if ( energiaSobranteCentral(cDestino) < consumoMasPerdidas(cDestino, cli) ) { // si ya no cabe el cliente
                        ultimo_cliente_assig = false;
                    } else {
                        n_clientes_asignados++;
                        proporcion_distancias_asignados += normalizaDistancia(cDestino, cli);

                        proporcion_ocupacion_encendidas -= normalizaOcupacion(cDestino);
                        energia_servida[cDestino] += consumoMasPerdidas(cDestino, cli);
                        proporcion_ocupacion_encendidas += normalizaOcupacion(cDestino);

                        beneficio += clientes.get(cli).getConsumo() * precioMwCliente(cli) + precioPenalizacion(cli);
                        clientes_asignados[cli] = cDestino;
                    }
                }
                cli++;
            }
        }
        else if (cOrigen != -1 && cDestino == -1){ // Desasignamos clientes de una cOrigen
            n_centrales_encendidas--;
            for (int cli = 0; cli < clientes_asignados.length; cli++) {
                if (clientes_asignados[cli] == cOrigen) {
                    n_clientes_asignados--;
                    proporcion_distancias_asignados -= normalizaDistancia(cOrigen, cli);

                    beneficio -= clientes.get(cli).getConsumo() * precioMwCliente(cli) + precioPenalizacion(cli);
                    clientes_asignados[cli] = -1;
                }
            }
            proporcion_ocupacion_encendidas -= normalizaOcupacion(cOrigen);
            energia_servida[cOrigen] = 0.0;

            beneficio -= costeCentralParada(cOrigen);
            beneficio += costeCentralEncendida(cOrigen);
        }
        else { // Volcamos una central a otra (ninguna esta fuera)
            proporcion_ocupacion_encendidas -= normalizaOcupacion(cOrigen);
            energia_servida[cOrigen] = 0.0;

            beneficio -= costeCentralParada(cOrigen);
            beneficio += costeCentralEncendida(cOrigen);
            n_centrales_encendidas--;
            if (energia_servida[cDestino] == 0.0) {
                beneficio += costeCentralParada(cDestino);
                beneficio -= costeCentralEncendida(cDestino);
                n_centrales_encendidas++;
            }

            proporcion_ocupacion_encendidas -= normalizaOcupacion(cDestino);
            for (int cli = 0; cli < clientes_asignados.length; cli++) {
                if (clientes_asignados[cli] == cOrigen) {
                    proporcion_distancias_asignados -= normalizaDistancia(cOrigen, cli);
                    proporcion_distancias_asignados += normalizaDistancia(cDestino, cli);

                    clientes_asignados[cli] = cDestino;
                    energia_servida[cDestino] += consumoMasPerdidas(cDestino,cli);
                }
            }
            proporcion_ocupacion_encendidas += normalizaOcupacion(cDestino);
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
        //
        if (cOrigen == -1) { // intentaremos asignar el maximo de clientes al cDestino
            return true;
        }
        //
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
                    sum += consumoMasPerdidas(cDestino, c);
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
//        for (int i_cliente = 0; i_cliente < clientes_asignados.length; i_cliente++) { // Centrales
//            System.out.println("Cliente " + i_cliente + " -> " + clientes_asignados[i_cliente] + " -> " + clientes.get(i_cliente).getConsumo()*precioMwCliente(i_cliente) + "->" + precioPenalizacion(i_cliente) + " -> " + clientes.get(i_cliente).getContrato());
//        }
        System.out.println("Beneficio: " + beneficio);
        // print que li he posat per veure la FH
        System.out.println("Heuristica: " + heuristicFunction());
        System.out.println("Profundiad: " + profundidad_arbol);
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

    private double normalizaDistancia (int i_central, int i_cliente) {
        return (141.421356 - distancias[i_central][i_cliente]) / 141.421356;
    }

    private double normalizaOcupacion (int i_central) {
        return (energia_servida[i_central] / centrales.get(i_central).getProduccion());
    }

    // Funcio heurística con precalculo
    public double heuristicFunction() {

        double proporcion_distancia = proporcion_distancias_asignados /n_clientes_asignados;
        double proporcion_ocupacion = proporcion_ocupacion_encendidas /n_centrales_encendidas;

        return beneficio * (0.4*proporcion_ocupacion + 0.6*proporcion_distancia);
    }

    public void printTiposCentralesEncendidas() {
        int n_tipoA = 0;
        int n_tipoB = 0;
        int n_tipoC = 0;

        for (int i = 0; i < energia_servida.length; i++) {
            if ( energia_servida[i] != 0 ) { // central encendida
                if (centrales.get(i).getTipo() == 0) { // central de tipo A
                    n_tipoA++;
                } else if (centrales.get(i).getTipo() == 1) { // central de tipo B
                    n_tipoB++;
                } else if (centrales.get(i).getTipo() == 2) { // central de tipo C
                    n_tipoC++;
                }
            }
        }

        System.out.println("Tipo A: " + n_tipoA);
        System.out.println("Tipo B: " + n_tipoB);
        System.out.println("Tipo C: " + n_tipoC);

    }

}
