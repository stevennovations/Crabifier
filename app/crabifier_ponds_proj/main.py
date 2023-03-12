from typing import Any, Dict, Optional
import os

from fastapi import Body, Request, FastAPI
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from google.cloud import firestore
from google.cloud import dialogflowcx_v3 as dialogflowcx
import pytz
from datetime import datetime

from dotenv import load_dotenv
from uuid import UUID, uuid4
import pytz

load_dotenv()
agent_id = os.getenv('AGENT_ID')
project_id = os.getenv('PROJECT_ID')
location = os.getenv('location')

# Set the model config file
os.environ["GOOGLE_APPLICATION_CREDENTIALS"]="config/credentials.json"

app = FastAPI()
db = firestore.Client()
ponds_collection = u'ponds'
ponds_ref = db.collection(ponds_collection)

class PondsBase(BaseModel):
    pond_name: str = None
    olivacea: int = None
    tranquebarica: int = None
    serrata: int = None
    description: Optional[str] = None
    # TODO:
    ## Add photos list for images 

class Ponds(PondsBase):
    pond_id: str
    created_at: datetime
    updated_at: datetime

class PondsCreate(PondsBase) :
    pond_id: str = (Field(default_factory=uuid4))
    created_at: datetime = Field(default_factory=datetime.utcnow)
    # now(pytz.timezone("Asia/Hong_Kong")))
    updated_at: datetime = Field(default_factory=datetime.utcnow)

    class Config:
        schema_extra = {
            "example": {
                "pond_name": "An Item",
                "olivacea": 0,
                "tranquebarica": 0,
                "serrata": 0,
                "description": "A quite common item.",
            }
        }

class PondsUpdate(PondsBase):
    updated_at: datetime = Field(default_factory=datetime.utcnow)

    class Config:
        schema_extra = {
            "example": {
                "pond_name": "An Item",
                "olivacea": 0,
                "tranquebarica": 0,
                "serrata": 0,
                "description": "A quite common item.",
            }
        }


@app.get("/")
def root():
    return "todooo"

@app.post("/ponds")
def create_pond(pond : PondsCreate):

    pond_item = pond.dict()
    print(pond_item)
    pid_str = str(pond_item['pond_id']).split('-')[-1]
    pond_item['pond_id'] = f'pid_{pid_str}'
    print(pond_item['pond_id'])

    doc_ref = db.collection(ponds_collection).document(pond_item['pond_id'])
    doc_ref.set(pond_item)

    return {"pond_id" : pond_item['pond_id']}

@app.get("/ponds/{pond_id}")
def read_pond(pond_id: str):
    doc_ref = db.collection(ponds_collection).document(str(pond_id))
    doc = doc_ref.get()
    if doc.exists:
        return Ponds(**doc.to_dict())
    return

@app.put("/ponds/{id}")
def update_pond(pond_id: str, pond_update : PondsUpdate):
    data = pond_update.dict()
    doc_ref = db.collection(ponds_collection).document(str(pond_id))
    doc_ref.update(data)
    return {"status_code" : 200, "message" : f'Updated {pond_id}'}

@app.delete("/ponds/{id}")
def delete_pond(pond_id: str):

    db.collection(ponds_collection).document(pond_id).delete()

    return {"message" : f"Deleted Pond: {pond_id}"}

@app.get("/ponds")
def read_pond_list():
    print("Get list of ponds")
    items_ref = db.collection(ponds_collection)
    return [
        Ponds(**doc.get().to_dict())
        for doc in items_ref.list_documents()
        if doc.get().to_dict()
    ]


## Add collection to your document
# db.collection('users').doc(this.username).collection('booksList').doc(myBookId).set({
#   password: this.password,
#   name: this.name,
#   rollno: this.rollno
# })