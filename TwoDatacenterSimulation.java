import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.*;

public class TwoDatacenterSimulation {

    public static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<>();

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(1000)));

        Host host = new Host(
                0,
                new RamProvisionerSimple(2048),
                new BwProvisionerSimple(10000),
                1000000,
                peList,
                new VmSchedulerTimeShared(peList)
        );

        hostList.add(host);

        DatacenterCharacteristics characteristics =
                new DatacenterCharacteristics(
                        "x86","Linux","Xen",hostList,
                        10.0,3.0,0.05,0.001,0.0
                );

        Datacenter datacenter = null;

        try {
            datacenter = new Datacenter(
                    name,
                    characteristics,
                    new VmAllocationPolicySimple(hostList),
                    new LinkedList<Storage>(),
                    0
            );
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    public static void main(String[] args) {

        try {

            CloudSim.init(1, Calendar.getInstance(), false);

            Datacenter dc1 = createDatacenter("Datacenter_1");
            Datacenter dc2 = createDatacenter("Datacenter_2");

            DatacenterBroker broker = new DatacenterBroker("Broker");
            int brokerId = broker.getId();

            // ---------- VMs ----------
            List<Vm> vmList = new ArrayList<>();

            vmList.add(new Vm(0, brokerId, 500, 1, 512, 1000, 10000,
                    "Xen", new CloudletSchedulerTimeShared()));

            vmList.add(new Vm(1, brokerId, 500, 1, 512, 1000, 10000,
                    "Xen", new CloudletSchedulerTimeShared()));

            broker.submitVmList(vmList);

            // ---------- CLOUDLETS ----------
            List<Cloudlet> cloudletList = new ArrayList<>();
            UtilizationModel model = new UtilizationModelFull();

            long[] lengths = {20000, 40000, 60000, 80000}; // different execution times

            for(int i = 0; i < 4; i++) {

                Cloudlet cloudlet = new Cloudlet(
                        i,
                        lengths[i],
                        1,
                        300,
                        300,
                        model,
                        model,
                        model
                );

                cloudlet.setUserId(brokerId);
                cloudletList.add(cloudlet);
            }

            broker.submitCloudletList(cloudletList);

            // ---------- RUN ----------
            CloudSim.startSimulation();
            List<Cloudlet> results = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            // ---------- OUTPUT ----------
            System.out.println("\n========== RESULT ==========");

            for (Cloudlet cl : results) {
                System.out.println(
                        "Cloudlet " + cl.getCloudletId()
                                + " | Length: " + cl.getCloudletLength()
                                + " | VM: " + cl.getVmId()
                                + " | Datacenter: " + cl.getResourceId()
                                + " | Time: " + cl.getActualCPUTime()
                );
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
