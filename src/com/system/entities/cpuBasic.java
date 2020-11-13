package com.system.entities;

import com.system.handlers.Tuple;
import com.system.handlers.enumCommands;
import com.system.handlers.enumState;

import javax.swing.plaf.synth.SynthSpinnerUI;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class cpuBasic {

    protected int PC;
    protected int Accumulator;

    protected int cpuStop;

    protected int[] memory;
    private int sizeProgram;

    /**
     *  First Integer: instruction index
     *  Second Integer: argument (if needed)
     */
    private final ArrayList<Tuple<Integer, Integer>> memoryInstructions = new ArrayList<>();

    private enumCommands tryEnum (String myString) {
        try {
            return Enum.valueOf(enumCommands.class, myString);
        } catch (IllegalArgumentException e) {
            // log error or something here
            return Enum.valueOf(enumCommands.class, "ERROR");
        }
    }
    public void insertInstruction(String[] myString) {
        for(String v : myString)
            insertInstruction(v);
        setSizeProgram();
    }
    public void insertInstruction(String myString) {
        String[] parsed = mySplit(myString, " ");

        if (parsed.length > 2)
            throwError(enumState.InvalidInstructions.getState());

        String cmd = parsed[0];
        enumCommands myEnum = tryEnum(cmd);
        //System.out.println(cmd + ", got: " + myEnum);

        if (parsed.length > 1)
            memoryInstructions.add(new Tuple<>(myEnum.getCommand(), Integer.parseInt(parsed[1])));
        else
            memoryInstructions.add(new Tuple<>(myEnum.getCommand(), null));

        setSizeProgram();
    }

    protected Tuple<Integer, Integer> getInstruction(int PC) {

        if (PC < getSizeProgram())
            return memoryInstructions.get(PC);
        else {
            this.PC--;
            return new Tuple<>(enumCommands.ERROR.getCommand(), null);
        }
    }

    private void CARGI(int n) {
        Accumulator = n;
    }
    private void CARGM(int n) {
        if(n < memory.length)
            Accumulator = memory[n];
        else
            throwError(2);
    }
    private void CARGX(int n) {
        if(n < memory.length) {
            int aux = memory[n];
            if (aux < memory.length) {
                Accumulator = memory[memory[n]];
                return;
            }
        }
        throwError(2);
    }
    private void ARMM(int n) {
        if(n < memory.length)
            memory[n] = Accumulator;
        else
            throwError(2);
    }
    private void ARMX(int n) {
        if(n < memory.length) {
            int aux = memory[n];
            if (aux < memory.length) {
                memory[memory[n]] = Accumulator;
                return;
            }
        }
        throwError(2);
    }
    private void SOMA(int n) {
        if(n < memory.length)
            Accumulator += memory[n];
        else
            throwError(2);
    }
    private void NEG() {
        Accumulator *= -1;
    }
    private void DESVZ(int n) {
        if ( Accumulator == 0 ) {
            PC = n;
        }
    }

    private void PARA() {
        cpuStop = enumState.Stop.getState();
        PC--;
    }
    private void LE() {
        throwError(enumState.Read.getState());
    }
    private void GRAVA() { throwError(enumState.Save.getState()); }
    private void ERROR() {
        throwError(enumState.InvalidInstructions.getState());
    }

    private interface instruction {
        void execute(Object i);
    }

    private final instruction[] getInstruction = new instruction[] {
            n -> CARGI((int) n),
            n -> CARGM((int) n),
            n -> CARGX((int) n),
            n -> ARMM((int) n),
            n -> ARMX((int) n),
            n -> SOMA((int) n),
            n -> NEG(),
            n -> DESVZ((int) n),
            n -> PARA(),
            n -> LE(),
            n -> GRAVA(),
            n -> ERROR(),
    };

    public static String[] mySplit(String str, String regex)
    {
        Vector<String> result = new Vector<>();
        int start = 0;
        int pos = str.indexOf(regex);
        while (pos>=start) {
            if (pos>start) {
                result.add(str.substring(start,pos));
            }
            start = pos + regex.length();
            pos = str.indexOf(regex,start);
        }
        if (start<str.length()) {
            result.add(str.substring(start));
        }
        return result.toArray(new String[0]);
    }

    private void throwError(int i) {
        cpuStop = i;
    }

    private boolean hasArgument(int i) {
        if (i == 6 || i == 8 || i == 9 || i == 10)
            return false;
        return true;
    }

    public void execute() {
        if(isCpuStop())
            return;

        Tuple<Integer, Integer> aux;
        if (PC < memoryInstructions.size())
            aux = memoryInstructions.get(PC);
        else {
            throwError(enumState.InvalidInstructions.getState());
            return;
        }
        int i = aux.getX();
        Object n = aux.getY();

        if(!hasArgument(i) && n != null || hasArgument(i) && n == null) {
            throwError(enumState.InvalidInstructions.getState());
            return;
        }
        PC++;
        getInstruction[i].execute(n);

        //System.out.println("value Pc: " + PC + ", instruction type: " + enumCommands.values()[i] + ", A: " + Accumulator/*+ ", memory: " + Arrays.toString(memory)*/);
    }

    public void popInstruction() {
        memoryInstructions.remove(memoryInstructions.get(memoryInstructions.size()-1));
        setSizeProgram();
        if(PC >= getSizeProgram())
            PC--;
    }

    public void clearInstructions() {
        memoryInstructions.clear();
        setSizeProgram();

        PC = 0;
        Accumulator=0;
        setCpuStop(enumState.Normal.getState());
    }

    protected int getSizeProgram() { return sizeProgram; }

    protected void setSizeProgram() { sizeProgram = memoryInstructions.size(); }

    public String getInstructions() {
        StringBuilder ret = new StringBuilder("");
        int i = 0;
        for(Tuple<Integer, Integer> v : memoryInstructions){
            ret.append(i).append(": ").append(enumCommands.values()[v.getX()]).append(" ");
            if (v.getY() == null)
                ret.append('\n');
            else
                ret.append(v.getY()).append('\n');
            i++;
        }
        return ret.toString();
    }

    public void setCpuStop(int i) {
        cpuStop = i;
    }

    public boolean isCpuStop() {
        return cpuStop != enumState.Normal.getState();
    }
}