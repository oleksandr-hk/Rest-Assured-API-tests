package requests.skelethon.interfaces;

import models.BaseModel;

public interface CrudEndPointInterface {
    Object post(BaseModel model);
    Object get(long id);
    Object update(BaseModel model);
    Object delete(long id);
}
