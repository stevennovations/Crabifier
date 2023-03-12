# Crabifier FastAPI Backend

This is the backend CRUD for Ponds in the new crabifier app.

## Installing Pre-requisites
These are the requirements you need for your python environment:
```
  python 3.8 and above 
  pip install -r requirements.txt
```

You would also need to create a service account for your firestore database in GCP. [Here is a tutorial](https://cloud.google.com/iam/docs/creating-managing-service-accounts#iam-service-accounts-create-console)

Add your service account `json` to a `config/` folder

Change this snippet to match the name of your service account filename
```
# Set the model config file
os.environ["GOOGLE_APPLICATION_CREDENTIALS"]="config/credentials.json"
```

## Running your FastAPI Project

### Run your FastAPI Project to be accessed on localhost
```
uvicorn main:app --reload
```

Access the project on `localhost:8000` and access the docs on `localhost:8000/docs`

### If you would want to deploy it from your local machine

You need to download ngrok from [here](https://ngrok.com/). After downloading and following the instructions:
```
./ngrok http 80
```

## Firestore DB

To be able to create a firestore DB.

1. Create a firestore db application on GCP
2. Create a single collection - preferrably the collection name inside `main.py`
3. The structure of the collection and its fields is represented in the `BaseModel` of the FastAPI Project

```

ponds_collection = u'ponds' # Change to the name of your collection
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
```