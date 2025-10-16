#!/bin/bash

# Run README Wrangler locally with local profile

echo "🚀 Starting README Wrangler (local profile)..."

mvn spring-boot:run -Dspring-boot.run.profiles=local
