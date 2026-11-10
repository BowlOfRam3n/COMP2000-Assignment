public class AutumnSeason implements SeasonStrategy {

    @Override
    public String getName(){
        return "AUTUMN";
    }

    @Override 
    public void modifyCell(Cell cell) {
        // Antivirus cells becokme 1.2x larger.
        if (cell.getState().equals(AntivirusState.INSTANCE)){
            cell.setSizeMultiplier(1.2);
        }
    }
}
