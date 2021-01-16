package com.system.entities;

import com.system.Main;
import com.system.handlers.VarsMethods;
import com.system.handlers.enumState;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

import static com.system.handlers.VarsMethods.initial_quantum;

public class Scheduler {

    private Process[] Jobs;

    private int processControl = 0;

    // benchmark
    protected int time_cpu = 0;

    protected long time_cpu_begin = 0;
    protected long total_time_cpu = 0;

    protected long time_idle_begin = 0;
    protected long total_time_idle = 0;

    protected int changes = 0;
    protected int preemption_times = 0;
    // -------------


    private boolean end = false;

    public Scheduler(String str) {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(str))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONArray jobs = (JSONArray) obj;

            Jobs = new Process[jobs.size()];

            int i = 0;
            for(Object job : jobs) {
                Jobs[i] = new Process((JSONObject) job);
                i++;
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void update_idle_time() {
        if(time_idle_begin < 0 )
            return;
        total_time_idle += System.nanoTime() - time_idle_begin;
        time_idle_begin = -1;
    }

    public int getProcessControl() {
        return processControl;
    }

    public void printJob() {
        Jobs[processControl].printAll();
    }

    public Process getCurrentProcess() {
        if (Jobs[processControl].date_release == -1) {
            Jobs[processControl].date_release = SO.timer.getTimer();
            Jobs[processControl].time_release = System.nanoTime();
        }
        return Jobs[processControl];
    }

    protected boolean isEnd() {
        return end;
    }

    public void unlockProcess(int i) {
        Jobs[i].blocked = false;
        Jobs[i].update_time_blocked();
    }

    public void block() {
        Jobs[processControl].blocked = true;
    }

    public void calculate_priority() {
        Jobs[processControl].setPriority((Jobs[processControl].getPriority() + (1d - ((double) SO.getQuantum()/initial_quantum)))/2d);
    }

    public int nextJob() {

        int index = -1;
        double highest_priority = 2;
        this.end = true;

        double temp = Jobs[processControl].getPriority();
        Jobs[processControl].setPriority(2d);

        for (int i = 0; i < Jobs.length; i++) {
            if (!Jobs[i].ended)
                this.end = false;
            if (!Jobs[i].blocked && Jobs[i].getPriority() < highest_priority) {
                index = i;
                highest_priority = Jobs[i].getPriority();
            }
        }

        Jobs[processControl].setPriority(temp);
        calculate_priority();

        if(index == processControl)
            return -1;
        if(index > -1) {
            processControl = index;
            changes++;
        }

        return index;

    }

    public boolean isCurrent(int i) {
        return Jobs[i] == getCurrentProcess();
    }

    public void setJobEnd(){
        Jobs[processControl].ended = true;
    }

    public void setProcessNormal(int i) {
        this.Jobs[i].getRegisters().State = enumState.Normal;
    }

    public void printResults() {
        int i = 0;
        for(Process job : Jobs) {
            System.out.printf("\nBenchmark Process %d:\n", i);
            System.out.println("Hora de Inicio: " + job.date_release + " ás " + ((job.time_release - VarsMethods.start) / 1000000d) + "ms");
            System.out.println("Hora de Termino: " + job.date_end + " ás " + ((job.time_end - VarsMethods.start) / 1000000d) + "ms");
            System.out.println("Tempo de Retorno: " + (job.date_end - job.date_release) + " ás " + ((job.time_end - job.time_release) / 1000000d) + "ms");
            System.out.println("Tempo de CPU: " + job.time_cpu + " ás " + (job.total_time_cpu / 1000000d) + "ms");
            System.out.println("Percentual de CPU: " + (job.time_cpu*100/this.time_cpu) + "% e " + (job.total_time_cpu*100/this.total_time_cpu) + "%");
            System.out.println("Tempo Bloqueado: " + job.time_blocked + " ás " + (job.total_time_blocked / 1000000d) + "ms");
            System.out.println("Vezes Bloqueado: " + job.blocked_times);
            System.out.println("Vezes Escalonado: " + job.times_schedule);
            System.out.println("Numero de vezes que a CPU foi perdida: " + job.times_lost);
            preemption_times += job.times_lost;
            i++;
        }
    }
}
