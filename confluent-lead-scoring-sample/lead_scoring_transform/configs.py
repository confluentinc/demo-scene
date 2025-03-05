from google.cloud import storage
import pickle


def load_pickle_file(pickle_filename: str):
    storage_client = storage.Client()
    bucket = storage_client.get_bucket("your-bucket-name")
    blob = bucket.get_blob(pickle_filename)
    pickle_in = blob.download_as_string()
    my_dictionary = pickle.loads(pickle_in)
    return my_dictionary