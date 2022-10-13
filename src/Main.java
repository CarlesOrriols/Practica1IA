import IA.Energia.Centrales;
import IA.Energia.Clientes;
import IA.Energia.VEnergia;

/*
*
* */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        VEnergia vEnergia = new VEnergia();

        int[] num_tipos_centrales = new int[] {5,10,25};
        int semilla_random = 100;
        Centrales centrales = new Centrales(num_tipos_centrales, semilla_random);
        Clientes clientes = new Clientes();

    }
}