DATABASES = {
    'default': {
        'ATOMIC_REQUESTS': True,
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': "awx",
        'USER': "awx",
        'PASSWORD': "awxpass",
        'HOST': "postgres",
        'PORT': "5432",
    }
}

BROADCAST_WEBSOCKET_SECRET = "YzpyVEM1cmtCY2JTdzEwczd5eS1XZWV3LjY1M2Z4NmZySkZsZzRWQlY0RnpuS05jRkdrdUVyd2x0aHpwVDloRzFZOG4yLkxOSHExN1dBXzouUWhCUi02cFRBc1N5SEVtZ2taanRnTWU4Tm1MTWpGSkFBRzE6aGhJdnlmZUF5T24="
