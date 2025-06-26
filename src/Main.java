import jscip.*;

import static jscip.SCIP_Vartype.SCIP_VARTYPE_BINARY;

public class Main {
    public static void main(String[] args) {

        System.load("/opt/JSCIPOpt/build/Release/libjscip.so");

        Scip scip = new Scip();
        scip.create("SchoolTimetable");

        // Definition des parametres du probleme
        int nbClasses = 2;
        int nbProfesseurs = 14;
        int nbMatieres = 15;     //15
        int nbCreneaux = 22;
        int nbSalles = 2;
        int nbHalve = 11;

        int[][] affect = {
                {2, 0}, {1, 1}, {2, 0}, {2, 0}, {2, 2}, {1, 0}, {2, 2}, {1, 1}, {1, 1}, {1, 1},
                {1, 1}, {1, 1}, {2, 0}, {0, 2}, {0, 2}
        };
        int[][][] Comb = {
                {{1, 0}, {0, 0}}, {{1, 0}, {0, 1}}, {{1, 0}, {0, 0}}, {{1, 0}, {0, 0}},
                {{1, 1}, {1, 1}}, {{1, 0}, {0, 0}}, {{1, 1}, {1, 1}}, {{1, 0}, {0, 1}},
                {{1, 0}, {0, 1}}, {{1, 1}, {1, 1}}, {{1, 1}, {1, 1}}, {{1, 1}, {1, 1}},
                {{1, 0}, {0, 0}}, {{0, 0}, {0, 1}}, {{0, 0}, {0, 1}}
        };

        int[][] DoesProf = {
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}
        };

        // Variables de decision: X[c][m][p][t][s] = 1 si la matiere m est enseignee a la classe c
        Variable[][][][][] X = new Variable[nbClasses][nbMatieres][nbProfesseurs][nbCreneaux][nbSalles];

        for (int c = 0; c < nbClasses; c++) {
            for (int m = 0; m < nbMatieres; m++) {
                for (int p = 0; p < nbProfesseurs; p++) {
                    for (int t = 0; t < nbCreneaux; t++) {
                        for (int s = 0; s < nbSalles; s++) {
                            X[c][m][p][t][s] = scip.createVar("X_" + c + "_" + m + "_" + p + "_" + t + "_" + s,
                                    0.0, 1.0, 0.0, SCIP_VARTYPE_BINARY);
                        }
                    }
                }
            }
        }
        // Variables de decision: X[c][m][p][t][s] = 1 si la matiere m est enseignee a la classe c
        Variable[][][][] Y = new Variable[nbMatieres][nbProfesseurs][nbCreneaux][nbSalles];

            for (int m = 0; m < nbMatieres; m++) {
                for (int p = 0; p < nbProfesseurs; p++) {
                    for (int t = 0; t < nbCreneaux; t++) {
                        for (int s = 0; s < nbSalles; s++) {
                            Y[m][p][t][s] = scip.createVar("Y_" + m + "_" + p + "_" + t + "_" + s,
                                    0.0, 1.0, 0.0, SCIP_VARTYPE_BINARY);
                        }
                    }
                }
            }

        Variable[][] Zc = new Variable[nbClasses][nbHalve];

        for (int c = 0; c < nbClasses; c++) {
            for (int h = 0; h < nbHalve; h++) {
                Zc[c][h] = scip.createVar("Z_" + c + "" + h, 0.0, 1.0, 1.0, SCIP_VARTYPE_BINARY);
            }

        }
    //------------------------------------------------------------------------------------------------------
        // 1
        for (int c = 0; c < nbClasses; c++) {
            for (int t = 0; t < nbCreneaux; t++) {
                Variable[] vars = new Variable[nbMatieres * nbSalles * nbProfesseurs];
                double[] coeffs = new double[nbMatieres * nbSalles * nbProfesseurs];
                int index = 0;
                for (int m = 0; m < nbMatieres; m++) {
                    for (int s = 0; s < nbSalles; s++) {
                        for (int p = 0; p < nbProfesseurs; p++) {
                            vars[index] = X[c][m][p][t][s];
                            coeffs[index] = 1.0;
                            index++;
                        }
                    }


                }

                Constraint const1 = scip.createConsLinear("Pchi smia" + "_", vars, coeffs, 0.0, 1.0);
                scip.addCons(const1);
            }
        }
        // 3 comb
        for (int m = 0; m < nbMatieres; m++) {
            for (int p = 0; p < nbProfesseurs; p++) {
                for (int s = 0; s < nbSalles; s++) {
                    for (int c = 0; c < nbClasses; c++) {
                        for (int t = 0; t < nbCreneaux; t++) {
                            Variable[] vars = new Variable[nbClasses+1];
                            double[] coeffs = new double[nbClasses+1];
                            int index = 0;
                            double sum_CombAndY = 0.0;
                            for (int c1 = 0; c1 < nbClasses; c1++) {
                                vars[index]=X[c1][m][p][t][s];
                                coeffs[index]=Comb[m][c][c1];
                                sum_CombAndY= sum_CombAndY+ Comb[m][c][c1];
                                index++;
                            }
                            vars[index]=Y[m][p][t][s];
                            coeffs[index] = -sum_CombAndY;
                            scip.addCons(scip.createConsLinear("activationY" + "_" + t, vars, coeffs, -Double.POSITIVE_INFINITY, 0.0));
                        }

                    }
                }
            }
        }

        // 4
        for (int p = 0; p < nbProfesseurs; p++) {
            for (int t = 0; t < nbCreneaux; t++) {
                Variable[] vars = new Variable[nbMatieres * nbSalles];
                double[] coeffs = new double[nbMatieres * nbSalles];
                int index = 0;
                for (int m = 0; m < nbMatieres; m++) {
                        for (int s = 0; s < nbSalles; s++) {
                            vars[index] = Y[m][p][t][s];
                            coeffs[index] = 1.0;
                            index++;

                        }


                }

                Constraint const4 = scip.createConsLinear("P4rofUniqu" + "_" + t, vars, coeffs, 0.0, 1.0);
                scip.addCons(const4);
            }
        }
        // 5 doesprof
        for (int p = 0; p < nbProfesseurs; p++) {
            for (int t = 0; t < nbCreneaux; t++) {
                for (int m = 0; m < nbMatieres; m++) {
                    Variable[] vars = new Variable[nbSalles];
                    double[] coeffs = new double[nbSalles];
                    int index = 0;
                        for (int s = 0; s < nbSalles; s++) {
                            vars[index] = Y[m][p][t][s];
                            coeffs[index] = 1.0;
                            index++;

                        }
                    Constraint const5 = scip.createConsLinear("Prof5Uu", vars, coeffs, 0.0, DoesProf[p][m] );
                    scip.addCons(const5);
                }


            }
        }


        // 6
        for (int s = 0; s < nbSalles; s++) {
            for (int t = 0; t < nbCreneaux; t++) {
                Variable[] vars1 = new Variable[nbMatieres * nbProfesseurs];
                double[] coeffs = new double[nbMatieres * nbProfesseurs];
                int index = 0;

                for (int m = 0; m < nbMatieres; m++) {
                    for (int p = 0; p < nbProfesseurs; p++) {
                        vars1[index] = Y[m][p][t][s];
                        coeffs[index] = 1.0;
                        index++;


                    }

                }
                Constraint constr6 = scip.createConsLinear("SalleU6nique1_" + "_" + t, vars1, coeffs, 0.0, 1.0);
                scip.addCons(constr6);
            }
        }

        // 7
        for (int m = 0; m < nbMatieres; m++) {
            for (int p = 0; p < nbProfesseurs; p++) {
                for (int s = 0; s < nbSalles; s++) {
                    for (int c = 0; c < nbClasses; c++) {
                        for (int t = 0; t < nbCreneaux; t++) {
                            Variable[] vars = new Variable[2];
                            double[] coeffs = new double[2];
                            vars[0] = X[c][m][p][t][s];
                            vars[1] = Y[m][p][t][s];
                            coeffs[0] = 1.0;
                            coeffs[1] = -1.0;
                            scip.addCons(scip.createConsLinear("activationY" + "_" + t, vars, coeffs, -Double.POSITIVE_INFINITY, 0.0));
                        }

                    }
                }
            }
        }

        // 8
        for (int m = 0; m < nbMatieres; m++) {
            for (int p = 0; p < nbProfesseurs; p++) {
                for (int s = 0; s < nbSalles; s++) {
                    for (int c1 = 0; c1 < nbClasses; c1++) {
                        for (int t = 0; t < nbCreneaux; t++) {
                            for (int c2 = 0; c2 < nbClasses; c2++) {
                                Variable[] vars = new Variable[2];
                                double[] coeffs = new double[2];
                                vars[0] = X[c1][m][p][t][s];
                                vars[1] = X[c2][m][p][t][s];
                                coeffs[0] = 1.0;
                                coeffs[1] = 1.0;
                                scip.addCons(scip.createConsLinear("activationY" + "_" + t, vars, coeffs, -Double.POSITIVE_INFINITY, 1 + Comb[m][c1][c2]));
                            }

                        }

                    }
                }
            }
        }
        // 2 affect
        for (int c = 0; c < nbClasses; c++) {
            for (int m = 0; m < nbMatieres; m++) {
                Variable[] vars = new Variable[nbCreneaux * nbProfesseurs * nbSalles];
                double[] coeffs = new double[nbCreneaux * nbProfesseurs * nbSalles];
                int index = 0;

                for (int t = 0; t < nbCreneaux; t++) {
                    for (int p = 0; p < nbProfesseurs; p++) {
                        for (int s = 0; s < nbSalles; s++) {
                        vars[index] = X[c][m][p][t][s];
                        coeffs[index] = 1.0;
                        index++;
                    }

                }

            }
                scip.addCons(scip.createConsLinear("SalleUnique_" + "_", vars, coeffs, affect[m][c], affect[m][c]+0.5));
            }
        }

        // 9
        for (int m = 0; m < nbMatieres; m++) {
            for (int p = 0; p < nbProfesseurs; p++) {
                for (int s = 0; s < nbSalles; s++) {
                    for (int c1 = 0; c1 < nbClasses; c1++) {
                        for (int t = 0; t < nbCreneaux; t++) {
                            for (int c2 = 0; c2 < nbClasses; c2++) {
                                Variable[] vars = new Variable[2];
                                double[] coeffs = new double[2];
                                vars[0] = X[c1][m][p][t][s];
                                vars[1] = X[c2][m][p][t][s];
                                coeffs[0] = 1.0;
                                coeffs[1] = -1.0;
                                scip.addCons(scip.createConsLinear("activationY" + "_" + t, vars, coeffs, -Double.POSITIVE_INFINITY, 1 - Comb[m][c1][c2]));
                            }

                        }

                    }
                }
            }
        }
        //ZZZ
        for (int c = 0; c < nbClasses; c++) {
            for (int h = 0; h < nbHalve; h++) {
                Variable[] vars = new Variable[1+ nbProfesseurs * nbSalles * nbMatieres ];
                double[] coeffs = new double[1+  nbProfesseurs * nbSalles * nbMatieres];
                int index = 0;
                for (int p = 0; p < nbProfesseurs; p++) {
                    for (int s = 0; s < nbSalles; s++) {
                        for (int m = 0; m < nbMatieres; m++) {
                            vars[index]=X[c][m][p][2*h+1][s];
                            coeffs[index] = 1.0;
                            index++;
                        }
                    }

                }
                vars[index]=Zc[c][h];
                coeffs[index] = -1.0;
                scip.addCons(scip.createConsLinear("ZZZ" + "_", vars, coeffs,-Double.POSITIVE_INFINITY ,0.0));

            }

        }

        for (int c = 0; c < nbClasses; c++) {
            for (int h = 0; h < nbHalve; h++) {
                Variable[] vars = new Variable[1+ nbProfesseurs * nbSalles * nbMatieres ];
                double[] coeffs = new double[1+  nbProfesseurs * nbSalles * nbMatieres];
                int index = 0;
                for (int p = 0; p < nbProfesseurs; p++) {
                    for (int s = 0; s < nbSalles; s++) {
                        for (int m = 0; m < nbMatieres; m++) {
                            vars[index]=X[c][m][p][2*h][s];
                            coeffs[index] = 1.0;
                            index++;
                        }
                    }

                }
                vars[index]=Zc[c][h];
                coeffs[index] = -1.0;
                scip.addCons(scip.createConsLinear("ZZZ" + "_", vars, coeffs,-Double.POSITIVE_INFINITY ,0.0));

            }

        }
        for (int c = 0; c < nbClasses; c++) {
            for (int h = 0; h < nbHalve; h++) {
                Variable[] vars = new Variable[1+2* nbSalles * nbProfesseurs * nbMatieres ];
                double[] coeffs = new double[1+2* nbSalles * nbProfesseurs * nbMatieres];
                int index = 0;
                for (int p = 0; p < nbProfesseurs; p++) {
                    for (int s = 0; s < nbSalles; s++) {
                        for (int m = 0; m < nbMatieres; m++) {
                            vars[2*index]=X[c][m][p][2*h][s];
                            coeffs[2*index] = -1.0;
                            vars[2*index+1]=X[c][m][p][2*h+1][s];
                            coeffs[2*index+1] = -1.0;
                            index++;
                        }
                    }

                }
                vars[2*index]=Zc[c][h];
                coeffs[2*index] = 1.0;
                scip.addCons(scip.createConsLinear("ZZZ" + "_", vars, coeffs,-Double.POSITIVE_INFINITY ,0.0));


            }

        }
        // Fonction objectif: Minimiser le nombre total de creneaux utilises
        Variable[] objVars = new Variable[nbClasses * nbMatieres * nbProfesseurs * nbCreneaux * nbSalles];
        double[] objCoeffs = new double[nbClasses * nbMatieres * nbProfesseurs * nbCreneaux * nbSalles];
        int index = 0;

        for (int c = 0; c < nbClasses; c++) {
            for (int m = 0; m < nbMatieres; m++) {
                for (int p = 0; p < nbProfesseurs; p++) {
                    for (int t = 0; t < nbCreneaux; t++) {
                        for (int s = 0; s < nbSalles; s++) {
                            objVars[index] = X[c][m][p][t][s];
                            objCoeffs[index] = 1.0;
                            index++;
                        }
                    }
                }
            }
        }
        scip.setMinimize();

        // Resolution du probleme
        scip.solve();
        // Affichage des resultats
        if (scip.getStatus() == SCIP_Status.SCIP_STATUS_OPTIMAL) {
            System.out.println("Solution optimale trouvee :");
            for (int c = 0; c < nbClasses; c++) {
                for (int m = 0; m < nbMatieres; m++) {
                    for (int p = 0; p < nbProfesseurs; p++) {
                        for (int t = 0; t < nbCreneaux; t++) {
                            for (int s = 0; s < nbSalles; s++) {
                                if (scip.getSolVal(scip.getBestSol(), X[c][m][p][t][s]) > 0.5) {
                                    System.out.println("Classe " + c + ", Matiere " + m + ", Professeur " + p + ", Creneau " + t + ", Salle " + s);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("Pas de solution optimale trouvee.");
        }
/*
        // Liberation de la memoire
        // Release all variables in X
        for (int c = 0; c < nbClasses; c++) {
            for (int m = 0; m < nbMatieres; m++) {
                for (int p = 0; p < nbProfesseurs; p++) {
                    for (int t = 0; t < nbCreneaux; t++) {
                        for (int s = 0; s < nbSalles; s++) {
                            scip.releaseVar(X[c][m][p][t][s]);
                        }
                    }
                }
            }
        }

// Release all variables in Y
        for (int m = 0; m < nbMatieres; m++) {
            for (int p = 0; p < nbProfesseurs; p++) {
                for (int t = 0; t < nbCreneaux; t++) {
                    for (int s = 0; s < nbSalles; s++) {
                        scip.releaseVar(Y[m][p][t][s]);
                    }
                }
            }
        }

 */

        // Free the SCIP problem
        //scip.free();


        // Store results in a 5D array
        int[][][][][] resultArray = new int[nbClasses][nbMatieres][nbProfesseurs][nbSalles][nbCreneaux];
        if (scip.getStatus() == SCIP_Status.SCIP_STATUS_OPTIMAL) {
            System.out.println("Optimal solution found:");
            for (int c = 0; c < nbClasses; c++) {
                for (int m = 0; m < nbMatieres; m++) {
                    for (int p = 0; p < nbProfesseurs; p++) {
                        for (int s = 0; s < nbSalles; s++) {
                            for (int t = 0; t < nbCreneaux; t++) {
                                double value = scip.getSolVal(scip.getBestSol(), X[c][m][p][t][s]);
                                resultArray[c][m][p][s][t] = (int)Math.round(value);
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("No optimal solution found.");
        }

        // Display results in desired 5D array format
        System.out.println("Results in array format:");
        StringBuilder resultBuilder = new StringBuilder("{");
        for (int c = 0; c < nbClasses; c++) {
            resultBuilder.append("{");
            for (int m = 0; m < nbMatieres; m++) {
                resultBuilder.append("{");
                for (int p = 0; p < nbProfesseurs; p++) {
                    resultBuilder.append("{");
                    for (int s = 0; s < nbSalles; s++) {
                        resultBuilder.append("{");
                        for (int t = 0; t < nbCreneaux; t++) {
                            resultBuilder.append(resultArray[c][m][p][s][t]);
                            if (t < nbCreneaux - 1) {
                                resultBuilder.append(",");
                            }
                        }
                        resultBuilder.append("}");
                        if (s < nbSalles - 1) {
                            resultBuilder.append(",");
                        }
                    }
                    resultBuilder.append("}");
                    if (p < nbProfesseurs - 1) {
                        resultBuilder.append(",");
                    }
                }
                resultBuilder.append("}");
                if (m < nbMatieres - 1) {
                    resultBuilder.append(",");
                }
            }
            resultBuilder.append("}");
            if (c < nbClasses - 1) {
                resultBuilder.append(",");
            }
        }
        resultBuilder.append("}");

        System.out.println(resultBuilder);

    }
}
