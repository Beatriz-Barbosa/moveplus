/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.moveplus;

import java.util.List;
import java.lang.*;
import java.text.*;
import java.util.Calendar;
import oshi.util.Util;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

/**
 * @author beatriz
 */
public class MovePlusOshi {

    //Criando uma nova classe de infos do Sistema
    SystemInfo si = new SystemInfo();
    OperatingSystem os = si.getOperatingSystem(); //pegando infos do OS do sistema
    HardwareAbstractionLayer hal = si.getHardware(); //Infos de Hardware do sistema
    List<HWDiskStore> dadosDisco = hal.getDiskStores(); //Uma lista de dados do disco do meu Hardware
    GlobalMemory memoria = hal.getMemory();          //Pego memória do meu hard
    CentralProcessor cpu = hal.getProcessor();      //E as informações da cpu
    long[] oldTricks = cpu.getSystemCpuLoadTicks();

    public void dadosHardware() {
        //Exibe os dados coletados
        System.out.println("---------------  Dados de CPU  ---------------");
        System.out.println(cpu);
        System.out.println("---------------  Sistema Operacional  ---------------");
        System.out.println(os);
        System.out.println("---------------  Memória RAM  ---------------");
        System.out.println(memoria);
        System.out.println("---------------  Disco  ---------------");
        System.out.println(dadosDisco);
        System.out.println("---------------  Processos  ---------------");
    }

    public String usoCpu() {

        //Variavel com o valor de uso da CPU
        Double pctCpu = cpu.getSystemCpuLoadBetweenTicks(oldTricks);
        //Convertendo o valor de uso da CPU 
        pctCpu = pctCpu * 100d;
        //Formatando o valor para string e trocando virgula por ponto
        String valorCpu = String.format("%.2f", pctCpu);
        valorCpu = valorCpu.replace(",", ".");
        return valorCpu;
    }

    public String dataHora() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(Calendar.getInstance().getTime());
        return timeStamp;
    }

    public void processos() {
        //Aqui só imprimindo o cabeçalho, esses números servem para dizer o espaçamento
        //positivos vão para direita e negativos para esquerda
        System.out.format("%4s %10s %8s %8s %7s", "ID", "Nome", "RAM", "CPU", "Bytes");

        //Coloco num loop para que ele me atualize 10x 
        for (int i = 0; i < 10; i++) {
            //Crio outro loop, iniciando minha variável process, e limitando ela a pegar 
            //somente X processos e ordena-los por MEMÓRIA  

            for (OSProcess process : os.getProcesses(5, OperatingSystem.ProcessSort.MEMORY)) {
                //Aqui vamos criar nossas variáveis de leitura        
                long ramMemory = process.getResidentSetSize() / 1024;             //RAM, no Linux RE
                double cpuUsage = process.getProcessCpuLoadBetweenTicks(process); //CPU    
                int processID = process.getProcessID();                           //ID do processo
                String processName = process.getName();                           //Nome do processo    
                long bytesRead = process.getBytesRead();                          //Bytes de Leitura do Disco    

                //Imprimindo os dados em formatação estilo "matriz"/tabela        
                System.out.format("\n %-8d %-8s %-8d %-8f %-10d", processID, processName, ramMemory, cpuUsage, bytesRead);
            }
            //Dando um delay nas execuções do for     
            Util.sleep(1000);
        }
    }

    public String usoRam() {
        //Código para pegar informações de RAM
        //Pegando o dado de total de RAM e convertendo para double
        long getTotalRam = memoria.getTotal();
        double totalRam = ((double) getTotalRam);

        //Pegando o dado de total disponível de RAM e convertendo para double
        long getAvailableRam = memoria.getAvailable();
        double availableRam = ((double) getAvailableRam);

        //Calculando a quantidade em uso e a porcentagem em uso de Ram
        double usedRam = totalRam - availableRam;
        double pctUsedRam = (usedRam / totalRam) * 100;
        String valorRam = String.format("%.2f", pctUsedRam);
        valorRam = valorRam.replace(",", ".");

        return valorRam;
    }

    public void usoDisco(int pid) {
        /**
         * ByteRead : Returns the number of bytes the process has read from
         * disk. ByteWritten : Returns the number of bytes the process has
         * written to disk.
         */
        OSProcess process;
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        process = os.getProcess(pid);
        System.out.println("\nDisk I/O Usage :");
        System.out.println("I/O Reads: " + process.getBytesRead());
        System.out.println("I/O Writes: " + process.getBytesWritten());
    }

    public void checkId(String id) {
        ConnectionDatabase config = new ConnectionDatabase();
        JdbcTemplate con = new JdbcTemplate(config.getDatasource());
        List<checkIdTerminal> query = con.query("SELECT idTerminal FROM "
                + "Terminal where idTerminal = " + id + ";", new BeanPropertyRowMapper(checkIdTerminal.class));

        if (query.size() > 0) {
            for (checkIdTerminal select : query) {
                System.out.println(select);
                System.out.println("ID VÁLIDO");
            }
        } else {
            System.out.println("ID INVÁLIDO");
        }

    }
    
    public void checkLogin(String user, String password) {
        ConnectionDatabase config = new ConnectionDatabase();
        JdbcTemplate con = new JdbcTemplate(config.getDatasource());
        List<checkIdTerminal> query = con.query("SELECT credencialUsuarioEstacao "
                + "FROM UsuarioEstacao "
                + "where credencialUsuarioEstacao = '" + user 
                + "' and senhaUsuarioEstacao = '" + password + "';", 
                new BeanPropertyRowMapper(checkIdTerminal.class));
        
        System.out.println(query);
        
        if (query.size() > 0) {
            for (checkIdTerminal select : query) {
                System.out.println(select);
                System.out.println("LOGIN VÁLIDO");
            }
        } else {
            System.out.println("LOGIN INVÁLIDO");
        }
    }
 
    public static void main(String[] args){
        //Instanciando a própria classe para usar seus métodos
        MovePlusOshi mpo = new MovePlusOshi();

        //Chamando a conexão com o Azure
        ConnectionDatabase config = new ConnectionDatabase();
        JdbcTemplate con = new JdbcTemplate(config.getDatasource());

        mpo.checkId("1");
        mpo.checkLogin("JFK12345","qwerty123");

        while (true) {
            String insert = "INSERT INTO Monitoracao (memoriaMonitoracao,"
                    + "cpuMonitoracao,discoMonitoracao,redeMonitoracao,"
                    + "dataHoraMonitoracao) values (" + mpo.usoRam() + ","
                    + mpo.usoCpu() + ",00.00,00.00, '" + mpo.dataHora() + "');";

            System.out.println("------------------------------");
            System.out.println(insert);
            System.out.println("RAM:        " + mpo.usoRam());
            System.out.println("CPU:        " + mpo.usoCpu());
            System.out.println("DATETIME:   " + mpo.dataHora());
            con.update(insert);
            Util.sleep(5000);
        }
    }
}
