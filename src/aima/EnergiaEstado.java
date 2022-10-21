package aima;

import IA.Energia.Centrales;
import IA.Energia.Clientes;
import IA.Energia.VEnergia;

import java.util.Random;

public class EnergiaEstado {
    private int[] clientes_assignados;
    private static Centrales centrales;
    private static Clientes clientes;

    public EnergiaEstado(int[] clientes_assignados) {
        this.clientes_assignados = clientes_assignados;
    }

    /*
    * Creamos un estado inicial assignando solo los clientes con prioridad garantizada en una central aleatoria,
    * controlando que la central tenga capacidad!
    */
    public static EnergiaEstado estadoInicial(Centrales ce, Clientes cl, int semilla) {
        centrales = ce;
        clientes = cl;
        EnergiaEstado estado = new EnergiaEstado(new int[centrales.size()]);

        Random random = new Random((long) semilla);

        for (int i_cliente = 0; i_cliente < estado.clientes_assignados.length; i_cliente++) { // Todos los clientes
            if ( clientes.get(i_cliente).getContrato() == 0 ) { // clientes garantizados
                boolean cli_asignado = false;
                while ( !cli_asignado ) { // Cliente de prioridad garantizada
                    int central_random = random.nextInt(centrales.size());
                    if (estado.sePuedeMoverCliente(i_cliente, central_random)) {
                        estado.moverCliente(i_cliente, central_random);
                        cli_asignado = true;
                    }
                }
            } else { // Clientes no garantizados
                estado.moverCliente(i_cliente, -1); // Colocamos fuera de ninguna central
            }
        }

        return estado;
    }


    /*
     * OPERADORES
     */
    // Pre: Que el cliente tenga lugar en la central destino o lo quiten de la central y sea NO garantizado
    public void moverCliente(int i_cliente, int centralDestino) {
        clientes_assignados[i_cliente] = centralDestino;
    }


    /*
     * COMPROVADORES
     */
    // Comprovamos que si la central destino es fuera, el cliente sea NO prioritario/garantizado,
    // de lo contrario tenga sitio para este cliente
    public boolean sePuedeMoverCliente(int i_cliente, int i_centralDestino) {
        if ( (i_centralDestino == -1 && clientes.get(i_cliente).getContrato() == 1) || consumoMasPerdidas(i_centralDestino, i_cliente) < energiaSobranteCentral(i_centralDestino) )
            return true;
        return false;
    }


    // Beneficio obtenido del estado
    public double beneficioTotal() {
        double sumaBeneficios = 0.0;
        for ( int i_central = 0; i_central < centrales.size(); i_central++) {
            sumaBeneficios += beneficioCentral(i_central);
        }
        return sumaBeneficios;
    }

    // Imprimir el estado
    public void print() {
        System.out.println("------ ESTADO -------");
        for (int i_cliente = 0; i_cliente < clientes_assignados.length; i_cliente++) { // Centrales
            System.out.println("Cliente " + i_cliente + " -> " + clientes_assignados[i_cliente]);
        }
        System.out.println("---------------------");
    }

    /*
     * FUNCIONES AUXILIARES
     */

    // Distancia entre 1 central y 1 cliente
    private double distanciaCentralCliente (int i_central, int i_cliente) {
        int ce_x = centrales.get(i_central).getCoordX();
        int ce_y = centrales.get(i_central).getCoordY();
        int cl_x = clientes.get(i_cliente).getCoordX();
        int cl_y = clientes.get(i_cliente).getCoordY();
        return Math.sqrt( Math.pow(ce_x-cl_x, 2) + Math.pow(ce_y-cl_y, 2)); // sqrt( dX^2 + dY^2)
    }

    // Suma de todas las distancias de los
    private double sumaTodasLasDistancias () {
        double sumDist = 0.0;
        for (int i_cli = 0; i_cli < clientes_assignados.length; i_cli++) {
            if ( clientes_assignados[i_cli] != -1 )
                sumDist += distanciaCentralCliente(i_cli, clientes_assignados[i_cli]);
        }
        return sumDist;
    }

    // Consumo de un cliente contando perdidas por la distancia a la central
    private double consumoMasPerdidas (int i_central, int i_cliente) {
        double dist = distanciaCentralCliente(i_central, i_cliente);
        return clientes.get(i_cliente).getConsumo() * (1+VEnergia.getPerdida(dist));
    }

    // Cantidad de energia que ya esta asignada
    private double energiaAssignadaCentral (int i_central) {
        double sumEnergiaUsada = 0.0;
        for (int i_cli = 0; i_cli < clientes_assignados.length; i_cli++) {
            if (clientes_assignados[i_cli] == i_central) {
                sumEnergiaUsada += consumoMasPerdidas(i_central, i_cli);
            }
        }
        return sumEnergiaUsada;
    }

    // Cantidad de energia que puede ser asignada aun
    private double energiaSobranteCentral (int i_central) {
        return centrales.get(i_central).getProduccion() - energiaAssignadaCentral(i_central);
    }

}
