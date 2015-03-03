
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class CalculateReachPrecisionAllCampaigns {

    private double viewsTotal = 0, views, conversions, conversionsTotal = 0;
    private ArrayList viewsAL = null, conversionsAL = null;
    private String campaignID = null;
    private double reachRequired;

    public void calculateRP (double reachRequired) {
        BufferedReader bufferedReader = null;
        String line = null, token = null;
        StringTokenizer st = null;
        boolean isFirstCampaign = true;
        this.reachRequired = reachRequired;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                line = bufferedReader.readLine();
                if (line == null) {
                    printRP();
                    break;
                }

                st = new StringTokenizer(line);
                if(line.contains("bin")) { //new campaign
                    if(!isFirstCampaign) {
                        printRP();
                    }
                    token = st.nextToken();
                    st = new StringTokenizer(token, ",");
                    st.nextToken();
                    st.nextToken();
                    campaignID = st.nextToken();
                    viewsTotal = 0;
                    conversionsTotal = 0;
                    viewsAL = new ArrayList();
                    conversionsAL = new ArrayList();
                    isFirstCampaign = false;
                    continue;
                }

                st = new StringTokenizer(line);
                st.nextToken();
                st.nextToken();
                st.nextToken();
                st.nextToken();

                views = Double.parseDouble(st.nextToken());
                st.nextToken();
                conversions = Double.parseDouble(st.nextToken());
                viewsTotal += views;
                conversionsTotal += conversions;

                viewsAL.add(views);
                conversionsAL.add(conversions);
            }
            bufferedReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void printRP() {
        int index;
        double reach = 0.0, precision = 0.0, viewsTotalCurrent = 0, conversionsTotalCurrent = 0, recall, FPR, prevRecall, prevFPR, base1, base2, altitude, area = 0;
        boolean isDone1 = false, isDone2 = false, isDone3 = false;
        ArrayList recallAL = new ArrayList(), FPRAL = new ArrayList();
        double reachPrint = 0.0, precisionPrint = 0.0;
        for(index = 0; index < viewsAL.size(); index++) {
            viewsTotalCurrent += (Double) viewsAL.get(index);
            conversionsTotalCurrent  += (Double) conversionsAL.get(index);
            reach = (viewsTotalCurrent / viewsTotal);
            precision = (conversionsTotalCurrent / viewsTotalCurrent);

            recall = conversionsTotalCurrent / conversionsTotal;
            FPR = (viewsTotalCurrent - conversionsTotalCurrent) / (viewsTotal - conversionsTotal);

            /*if(reach <= reachRequired) {
                recallAL.add(recall);
                FPRAL.add(FPR);
                precisionPrint = precision;
                reachPrint = reach;
            }*/


            if(reach > reachRequired && !isDone1) {
                System.out.println(campaignID + "\t" + viewsTotal + "\t" + conversionsTotal + "\t" + reach + "\t" + precision);
                isDone1 = true;
            }
            /*if(reach > 0.02 && !isDone1) {
                System.out.println(campaignID + "\t" + viewsTotal + "\t" + conversionsTotal + "\t" + reach + "\t" + precision);
                isDone1 = true;
            }
            else if(reach > 0.1 && !isDone2) {
                System.out.println(campaignID + "\t" + viewsTotal + "\t" + conversionsTotal + "\t" + reach + "\t" + precision);
                isDone2 = true;
            }
            else if(reach > 0.2 && !isDone3) {
                System.out.println(campaignID + "\t" + viewsTotal + "\t" + conversionsTotal + "\t" + reach + "\t" + precision);
                isDone3 = true;
            }*/
        }

        /*prevRecall = 0;
        prevFPR = 0;
        if (!recallAL.isEmpty()) {
            base1 = prevRecall;
            base2 = (Double) recallAL.get(0);
            altitude = (Double) FPRAL.get(0) - prevFPR;

            area += ((base1 + base2) * altitude) / 2;

            prevRecall = base2;
            prevFPR = (Double) FPRAL.get(0);



            for (index = 1; index < recallAL.size(); index++) {
                base1 = prevRecall;
                base2 = (Double) recallAL.get(index);
                altitude = (Double) FPRAL.get(index) - prevFPR;
                area += ((base1 + base2) * altitude) / 2;

                prevRecall = base2;
                prevFPR = (Double) FPRAL.get(index);
            }

            if (((Double) FPRAL.get(index - 1)) != reachRequired) {
                base1 = prevRecall;
                base2 = prevRecall;
                altitude = reachRequired - prevFPR;
                area += ((base1 + base2) * altitude) / 2;
            }
        }

        System.out.println(campaignID + "\t" + viewsTotal + "\t" + conversionsTotal + "\t" + reachPrint + "\t" + precisionPrint + "\t" + area);*/
    }

    public static void main(String[] args) {
        CalculateReachPrecisionAllCampaigns cRPAC = new CalculateReachPrecisionAllCampaigns();
        cRPAC.calculateRP(Double.parseDouble(args[0]));
    }
}

