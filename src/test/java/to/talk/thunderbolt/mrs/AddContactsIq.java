package to.talk.thunderbolt.mrs;

public class AddContactsIq extends Iq {

    AddContacts addContacts;

    public AddContacts getAddContacts() {
        return addContacts;
    }

    public void setAddContacts(AddContacts addContacts) {
        this.addContacts = addContacts;
    }
}
