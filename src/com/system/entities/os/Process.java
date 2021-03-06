package com.system.entities.os;

import com.system.entities.hardware.CPU;
import com.system.entities.hardware.Registers;
import com.system.entities.memory.PagesTable;
import com.system.handlers.Tuple;
import com.system.handlers.enumState;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Process {

    private final PagesTable pagesTable;
    private final int id;

    protected boolean blocked = false;
    protected boolean ended = false;

    private double priority;

    private final ArrayList<Tuple<Integer, Integer>> instructions = new ArrayList<>();

    private Registers registers;

    private int interruptions = 0;

    // Benchmark
    protected int date_release = -1;
    protected int date_end = -1;

    protected int time_cpu = 0;

    protected int blocked_times = 0;
    protected int times_schedule = 0;
    protected int times_lost = 0;
    protected int times_pageFault = 0;
    protected int times_blocked_MissingPage;

    protected int time_blocked_pageFault = 0;
    protected int time_blocked_IO = 0;
    protected int timeBlocked_missingPage = 0;
    protected int IOCalls = 0;
    // ----------------

    private final int sizeProgram;

    private final int[][] IO;
    private final int[] counter;
    private final int[] cost;

    public Process(JSONObject obj, int id) {

        this.id = id;

        registers = new Registers();

        JSONObject jobObject = (JSONObject) obj.get("job");

        JSONArray inst = (JSONArray) jobObject.get("program");
        priority = Double.parseDouble((String) jobObject.get("priority"));
        //memory = new int[Integer.parseInt((String) jobObject.get("memory"))];

        int sizeMem = Integer.parseInt((String) jobObject.get("IOSize"));
        JSONArray memObj = (JSONArray) jobObject.get("IO");

        int qtdMem = memObj.size();
        IO = new int[qtdMem][sizeMem];
        counter = new int[qtdMem];
        cost = new int[qtdMem];

        int i = 0;
        for (Object mem : memObj) {
            JSONObject objectMem = (JSONObject) mem;
            cost[i] =  Integer.parseInt((String) objectMem.get("cost"));
            int j = 0;
            for(Object data : (JSONArray) objectMem.get("data")) {
                IO[i][j] = Integer.parseInt((String) data);
                j++;
            }
            i++;
        }

        inst.forEach(str -> CPU.insertInstruction((String) str, this.instructions, registers));

        sizeProgram = instructions.size();

        pagesTable = new PagesTable(SO.NUM_PAGE, SO.SIZE_PAGE, id);
    }

    public void printAll() {

        System.out.println("estado: " + registers.getState());
        System.out.println("prioridade: " + priority);
        System.out.println("PC: " + registers.getPC());
        System.out.println("A: " + registers.getAccumulator());
        System.out.println("size: " + sizeProgram);
        System.out.print("IO: ");

        for(int[] j : IO)
            for(int i : j)
            System.out.print(i + " ");
        System.out.println();

        System.out.print("counter: ");
        for(int i : counter)
            System.out.print(i + " ");
        System.out.println();

        System.out.print("cost: ");
        for(int i : cost)
            System.out.print(i + " ");
        System.out.println();

        for(int i = 0; i < sizeProgram; i++) {
            System.out.print(instructions.get(i).getX() + " ");
            System.out.println(instructions.get(i).getY());
        }
    }

    public void addInterruption() {
        this.interruptions++;
    }
    protected void subInterruption() {
        this.interruptions--;
    }
    protected int getInterruption() {
        return this.interruptions;
    }

    public int getId() {
        return id;
    }

    public Registers getRegisters() {
        return this.registers;
    }

    public int[][] getIO() {
        return IO;
    }

    public int[] getCost() {
        return cost;
    }

    public int[] getCounter() {
        return counter;
    }

    public ArrayList<Tuple<Integer, Integer>> getInstructions() {
        return instructions;
    }

    public enumState getState() {
        return registers.getState();
    }

    public double getPriority() {
        return priority;
    }

    protected void setRegisters(Registers reg) {
        this.registers = reg;
    }

    protected void setPriority(double i) {
        this.priority = i;
    }

    public PagesTable getPagesTable() { return pagesTable; }
}
