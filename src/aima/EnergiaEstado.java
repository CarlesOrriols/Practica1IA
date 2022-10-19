package aima;

import IA.Energia.Centrales;
import IA.Energia.Clientes;
import IA.Energia.VEnergia;
import com.sun.tools.javac.Main;

import java.util.ArrayList;
import java.util.Random;

public class EnergiaEstado {
    private ArrayList<ArrayList<Integer>> centrales_con_clientes;
    private static Centrales centrales;
    private static Clientes clientes;

    public EnergiaEstado(ArrayList<ArrayList<Integer>> centrales_con_clientes) {
        this.centrales_con_clientes = centrales_con_clientes;
    }

    /*
    * Creamos un estado inicial assignando solo los clientes con prioridad garantizada en una central aleatoria,
    * controlando que la central tenga capacidad!
    */
    public static EnergiaEstado estadoInicial(Centrales ce, Clientes cl, int semilla) {
        centrales = ce;
        clientes = cl;
        EnergiaEstado estado = new EnergiaEstado(new ArrayList<ArrayList<Integer>>());
        estado.centrales_con_clientes = new ArrayList<ArrayList<Integer>>();

        // Inicializar centrales sin ningun cliente assignado
        for (int i = 0; i < centrales.size(); i++) {
            estado.centrales_con_clientes.add(new ArrayList<Integer>());
        }

        Random random = new Random((long) semilla);
        for (int i_cliente = 0; i_cliente < clientes.size(); i_cliente++) { // Todos los clientes
            if (clientes.get(i_cliente).getContrato() == 0) { // Cliente de prioridad garantizada
                boolean cliente_assignado = false;
                while ( !cliente_assignado ) {
                    int central_random = random.nextInt(centrales.size());
                    cliente_assignado = estado.anadir_cliente();
                }
            }
        }

        return estado;
    }


    /*
     * OPERADORES
     */
    public boolean anadir_cliente () {

        return true;
    }

    public boolean quitar_cliente (int central, int cliente) {

        return true;
    }

    public boolean intercanviar_cliente () {

        return true;
    }

    /*
    * Beneficio del estado
    */
    public double beneficioTotal() {
        VEnergia vEnergia;
        int gasto = 0;
        int ingreso = 0;
        for (int i_central = 0; i_central < centrales_con_clientes.size(); i_central++) {
            int tipo_central = centrales.get(i_central).getTipo();

            if (centrales_con_clientes.get(i_central).size() != 0) { // Tiene alguna central asignada
                // Esta en funcionamiento
                try {
                    double produccion_central = centrales.get(i_central).getProduccion();
                    double costeMW = VEnergia.getCosteProduccionMW(tipo_central);
                    double costeEncender = VEnergia.getCosteMarcha(tipo_central);
                    gasto += produccion_central * costeMW + costeEncender;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Esta apagada
                try {
                    double costeApagada = VEnergia.getCosteParada(tipo_central);
                    gasto += costeApagada;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            /*
             *  FALTAN LAS PENALIZACIONES DE LOS NO GARANTIZADOS !!!!!!!
             */

            for (int i_cliente = 0; i_cliente < centrales_con_clientes.get(i_central).size(); i_cliente++) {
                int tipo_cliente = clientes.get(i_cliente).getTipo();
                int contrato_cliente = clientes.get(i_cliente).getContrato();
                double precio_mw = 0;
                try {
                    precio_mw = clientes.get(i_cliente).getContrato() == 1 ? VEnergia.getTarifaClienteNoGarantizada(tipo_cliente) : VEnergia.getTarifaClienteGarantizada(tipo_cliente);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                double consumo = clientes.get(i_cliente).getConsumo();

                ingreso += consumo * precio_mw;
            }
        }
        return ingreso - gasto;
    }

    /*
     * Distancia entre 1 central y 1 cliente
     */
    private double distanciaCentralCliente(int i_central, int i_cliente) {
        int ce_x = centrales.get(i_central).getCoordX();
        int ce_y = centrales.get(i_central).getCoordY();
        int cl_x = clientes.get(i_cliente).getCoordX();
        int cl_y = clientes.get(i_cliente).getCoordY();
        return Math.sqrt( Math.pow(ce_x-cl_x, 2) + Math.pow(ce_y-cl_y, 2)); // sqrt( dX^2 + dY^2)
    }

    public void print() {
        System.out.println("------ ESTADO -------");
        for (int i_central = 0; i_central < centrales_con_clientes.size(); i_central++) { // Centrales
            System.out.println("Central " + i_central + ":");
            for (int i_cliente = 0; i_cliente < centrales_con_clientes.get(i_central).size(); i_cliente++) { // Clientes de esa central
                System.out.println("\tCliente " + i_cliente);
            }
        }
        System.out.println("---------------------");
    }
}
