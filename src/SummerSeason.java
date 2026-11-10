public class SummerSeason implements SeasonStrategy {
    
    @Override 
    public String getName(){
        return "SUMMER";
    }

    @Override
    public void modifyCell(Cell cell){
        // Infected cells move 1.4x faster.
        if (cell.getState().equals(InfectedState.INSTANCE)){
            cell.setSpeedMultiplier(1.4);
        }
    }
}