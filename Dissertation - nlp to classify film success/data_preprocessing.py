import pandas as pd
from sklearn.preprocessing import MinMaxScaler
from sklearn.model_selection import train_test_split
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem import WordNetLemmatizer

# Download necessary NLTK resources
nltk.download('punkt')
nltk.download('stopwords')
nltk.download('wordnet')

# Define preprocessing for textual data
stop_words = set(stopwords.words('english'))
lemmatizer = WordNetLemmatizer()

def preprocess_text(description):
    """Preprocesses text data: tokenization, lowercasing, stopword removal, and lemmatization."""
    tokens = word_tokenize(description.lower())  # Tokenize and lowercase
    tokens = [word for word in tokens if word.isalnum()]  # Remove punctuation
    tokens = [word for word in tokens if word not in stop_words]  # Remove stopwords
    tokens = [lemmatizer.lemmatize(word) for word in tokens]  # Lemmatize
    return ' '.join(tokens)

# Load the dataset
file_path = 'IMDB-Movie-Data.csv'  # Replace with your dataset file path
data = pd.read_csv(file_path)

# Check for missing values and drop rows with missing Revenue or Description
data = data.dropna(subset=['Revenue', 'Description'])

# Fill missing numerical values (e.g., Runtime) with the median
data['Runtime'] = data['Runtime'].fillna(data['Runtime'].median())

# Normalize numerical features
scaler = MinMaxScaler()
data[['Votes', 'Runtime']] = scaler.fit_transform(data[['Votes', 'Runtime']])

# One-hot encode categorical features (Genre)
genres = data['Genre'].str.get_dummies(sep=',')
data = pd.concat([data, genres], axis=1)
data = data.drop('Genre', axis=1)  # Drop the original Genre column

# Preprocess textual data (Description)
data['Processed_Description'] = data['Description'].apply(preprocess_text)

# Drop original Description column (optional, as we've processed it)
data = data.drop('Description', axis=1)

# Define features (X) and target (y)
X = data.drop(['Revenue'], axis=1)  # Drop the target column
y = data['Revenue']

# Split data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Save the preprocessed datasets for reuse
X_train.to_csv('X_train.csv', index=False)
X_test.to_csv('X_test.csv', index=False)
y_train.to_csv('y_train.csv', index=False)
y_test.to_csv('y_test.csv', index=False)

# Save the full preprocessed dataset for reference
data.to_csv('preprocessed_data.csv', index=False)

print("Preprocessing complete. Files saved:")
print("- Preprocessed dataset: preprocessed_data.csv")
print("- Training features: X_train.csv")
print("- Testing features: X_test.csv")
print("- Training labels: y_train.csv")
print("- Testing labels: y_test.csv")