package com.example.raindrops;

import java.util.Random;

public class Calculation {

    private int operand1;
    private int operand2;
    private int result;
    private String calculationQuestion;

    public String getCalculationQuestion() {
        Random random = new Random();
        int operationSign = random.nextInt(4);

        switch (operationSign)
        {
            case 0: operand1 = random.nextInt(20);
                operand2 = random.nextInt(20);
                result = operand1 + operand2;
                calculationQuestion = operand1 + "\n+" + operand2;
                break;
            case 1: operand1 = random.nextInt(20);
                operand2 = random.nextInt(20);
                if (operand1 < operand2){
                    int aux = operand1;
                    operand1 = operand2;
                    operand2 = operand1;
                }
                result = operand1 - operand2;
                calculationQuestion = operand1 + "\n-" + operand2;
                break;
            case 2: operand1 = random.nextInt(10);
                operand2 = random.nextInt(10);
                result = operand1 * operand2;
                calculationQuestion = operand1 + "\n*" + operand2;
                break;
            case 3: result = random.nextInt(10);      // operand2 cannot be 0
                operand2 = random.nextInt(10) + 1;   // to divide evenly instead of generating random numbers until operand1 % operand2 == 0,
                operand1 = result * operand2;               // we can just multiply 2 random numbers and set operand1 to the result of the multiplication
                calculationQuestion = operand1 + "\n/" + operand2;
                break;
            default:
                result = 0;
                calculationQuestion = "";
        }
        return calculationQuestion;
    }

    public int getCalculationResult(){
        return result;
    }
}
