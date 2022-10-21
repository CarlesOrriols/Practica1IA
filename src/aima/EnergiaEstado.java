package aima;

import IA.Energia.Centrales;
import IA.Energia.Clientes;
import IA.Energia.VEnergia;

import java.util.Random;

public class EnergiaEstado {
    private int[] clientes_asignados;
    private double beneficio;
    private double[] energia_ocupada;
    private static Centrales centrales;
    private static Clientes clientes;
    private static double[][] distancias; // [central][cliente]

    public EnergiaEstado(int[] clientes_assignados, double beneficio, double[] energia_ocupada) {
        this.clientes_asignados = clientes_assignados;
        this.beneficio = beneficio;
        this.energia_ocupada = energia_ocupada;
    }

    /*
    * Creamos un estado inicial assignando solo los clientes con prioridad garantizada en una central aleatoria,
    * controlando que la central tenga capacidad!
    */
    public static EnergiaEstado estadoInicial(Centrales ce, Clientes cl, int semilla) {
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

        double beneficio = 0.0;     // beneficio si no hay ningun cliente asignado (mas abajo restamos la penalizacion de los clientes no asignados)
        double[] energia_ocupada = new double[ce.size()];
        for (int i = 0; i < energia_ocupada.length; i++) { // inicializamos todas las energias ocupadas a 0
            energia_ocupada[i] = 0.0;
        }
        EnergiaEstado estado = new EnergiaEstado(new int[centrales.size()], beneficio, energia_ocupada);


        Random random = new Random((long) semilla);

        // Pre-inicializamos todos los clientes sin asignar a ninguna central
        for (int i_cliente = 0; i_cliente < estado.clientes_asignados.length; i_cliente++) {
            estado.clientes_asignados[i_cliente] = -1;
        }

        // Asignamos los clientes a una central random, si no puede ser asignado por una restriccion del problema, se asigna a otra tambien de forma aleatoria
        for (int i_cliente = 0; i_cliente < estado.clientes_asignados.length; i_cliente++) {
            if ( clientes.get(i_cliente).getContrato() == 0 ) { // clientes garantizados se asignan a una central
                boolean cli_asignado = false;
                while ( !cli_asignado ) {
                    int central_random = random.nextInt(centrales.size());
                    if (estado.sePuedeMoverCliente(i_cliente, central_random)) {
                        estado.moverCliente(i_cliente, central_random);
                        cli_asignado = true;
                    }
                }
            } else { // clientes no garantizados se actualiza el beneficio con la penalizacion pertinente
                try {
                    estado.beneficio -= estado.precioPenalizacion(i_cliente);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return estado;
    }


    /*
     * OPERADORES
     */
    // Pre: Que el cliente tenga lugar en la central destino o lo quiten de la central y sea NO garantizado
    public void moverCliente(int i_cliente, int centralDestino) {
        // Actualizar energia asignada
        int centralAntigua = clientes_asignados[i_cliente];
        if ( centralAntigua != -1 ) {       // Ya estaba asignado
            energia_ocupada[centralAntigua] -= consumoMasPerdidas(centralAntigua, i_cliente);
        }
        if ( centralDestino != -1 ) {       // Si no estamos desassignando el cliente
            energia_ocupada[centralDestino] += consumoMasPerdidas(centralDestino, i_cliente);
        }

        // Actualizar el beneficio (parte de coste de tener la central)
        if ( centralAntigua != -1 && energia_ocupada[centralAntigua] == 0) // La central antigua queda vacia
            beneficio += costeCentralParada(centralAntigua) - costeCentralEncendida(centralAntigua); // Apagamos central antigua
        if ( centralDestino != -1 && energia_ocupada[centralDestino] == consumoMasPerdidas(centralDestino, i_cliente)) // La central destino solo hay assignada la energia del cliente en transicion
            beneficio -= costeCentralParada(centralDestino) - costeCentralEncendida(centralDestino); // Encendemos central destino

        // Actualizar beneficio (parte que paga el cliente)
        if ( centralAntigua == -1 && centralDestino != -1 ) { // Cliente NO estaba asignado y pasa a asignado
            beneficio += clientes.get(i_cliente).getConsumo() * precioMwCliente(i_cliente);
        } else if ( centralAntigua != -1 && centralDestino == -1 ) { // Cliente estaba asignado y pasa a NO asignado
            beneficio -= clientes.get(i_cliente).getConsumo() * precioMwCliente(i_cliente);
        }

        // Actualizar clientes asignados
        clientes_asignados[i_cliente] = centralDestino;
    }




    /*
     * COMPROVADORES
     */
    // Comprovamos que si la central destino es fuera, el cliente sea NO prioritario/garantizado,
    // de lo contrario tenga sitio para este cliente
    public boolean sePuedeMoverCliente(int i_cliente, int i_centralDestino) {
        // (Si lo queremos desasignar y es no garantizado) o cabe en la central destino
        if ( (i_centralDestino == -1 && clientes.get(i_cliente).getContrato() == 1) || consumoMasPerdidas(i_centralDestino, i_cliente) <= energiaSobranteCentral(i_centralDestino) )
            return true;
        return false;
    }




    // Beneficio obtenido del estado
    public double beneficioTotal() {
        return beneficio;
    }

    // Imprimir el estado
    public void print() {
        System.out.println("------ ESTADO -------");
        for (int i_cliente = 0; i_cliente < clientes_asignados.length; i_cliente++) { // Centrales
            System.out.println("Cliente " + i_cliente + " -> " + clientes_asignados[i_cliente]);
        }
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

    // Cantidad de energia que ya esta asignada
    private double energiaAssignadaCentral (int i_central) {
        return energia_ocupada[i_central];
    }

    // Cantidad de energia que puede ser asignada aun
    private double energiaSobranteCentral (int i_central) {
        return centrales.get(i_central).getProduccion() - energiaAssignadaCentral(i_central);
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
            return VEnergia.getTarifaClientePenalizacion(clientes.get(i_cliente).getTipo());
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

}
