package clientManagement;

public class Client {






    public enum ClientStatus{
        UNAUTHENTICATED,        //new connection. not yet logged in
        CHARACTER_CREATION,     //creating a new character. not yet bound to an account
        DESPAWNED,              //character has died and not respawned at a location yet. Also new account that has yet to spawn in
        ACTIVE                  //active in the world
    }
}
